package com.example.weatherapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.ForecastResponse
import com.example.weatherapp.data.WeatherResponse
import com.example.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import retrofit2.HttpException
import java.io.IOException
import java.net.UnknownHostException

// ─────────────────────────────────────────────────────────────────────────────
// Theme mode enum — shared between ViewModel and UI
// ─────────────────────────────────────────────────────────────────────────────

enum class ThemeMode(val label: String) {
    Dark("Dark"),
    System("System"),
    Light("Light")
}

// ─────────────────────────────────────────────────────────────────────────────
// UI State
// ─────────────────────────────────────────────────────────────────────────────

sealed class WeatherUiState {
    object Idle    : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(
        val weather:  WeatherResponse,
        val forecast: ForecastResponse,
        val lat: Double = 0.0,
        val lon: Double = 0.0
    ) : WeatherUiState()
    data class Error(
        val message: String,
        val lat: Double = 0.0,
        val lon: Double = 0.0
    ) : WeatherUiState()
}

// ─────────────────────────────────────────────────────────────────────────────
// App settings
// ─────────────────────────────────────────────────────────────────────────────

data class AppSettings(
    val useCelsius:      Boolean   = true,
    val use24Hour:       Boolean   = false,
    val notificationsOn: Boolean   = false,
    val themeMode:       ThemeMode = ThemeMode.Dark
)

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState  = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    // ── Data loading ─────────────────────────────────────────────────────────

    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                supervisorScope {
                    val weatherDeferred  = async { repository.getWeatherByCoords(lat, lon) }
                    val forecastDeferred = async { repository.getForecastByCoords(lat, lon) }
                    _uiState.value = WeatherUiState.Success(
                        weather  = weatherDeferred.await(),
                        forecast = forecastDeferred.await(),
                        lat      = lat,
                        lon      = lon
                    )
                }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(friendlyError(e), lat, lon)
            }
        }
    }

    fun loadWeatherByCity(city: String) {
        val trimmed = city.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                supervisorScope {
                    val weatherDeferred  = async { repository.getWeatherByCity(trimmed) }
                    val forecastDeferred = async { repository.getForecastByCity(trimmed) }
                    _uiState.value = WeatherUiState.Success(
                        weather  = weatherDeferred.await(),
                        forecast = forecastDeferred.await()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error(friendlyError(e))
            }
        }
    }

    fun setLocationError(message: String) {
        _uiState.value = WeatherUiState.Error(message)
    }

    fun retry(lat: Double, lon: Double) {
        if (lat != 0.0 || lon != 0.0) loadWeather(lat, lon)
    }

    // ── Settings toggles ─────────────────────────────────────────────────────

    fun toggleCelsius()       { _settings.value = _settings.value.copy(useCelsius      = !_settings.value.useCelsius) }
    fun toggle24Hour()        { _settings.value = _settings.value.copy(use24Hour       = !_settings.value.use24Hour) }
    fun toggleNotifications() { _settings.value = _settings.value.copy(notificationsOn = !_settings.value.notificationsOn) }
    fun setThemeMode(mode: ThemeMode) { _settings.value = _settings.value.copy(themeMode = mode) }

    // ── City tracking ─────────────────────────────────────────────────────────

    private var lastSearchedCity = ""
    fun loadWeatherByCityTracked(city: String) {
        lastSearchedCity = city.trim()
        loadWeatherByCity(city)
    }

    // ── Error messages ────────────────────────────────────────────────────────

    private fun friendlyError(e: Exception): String = when (e) {
        is HttpException -> when (e.code()) {
            404  -> "\"${lastSearchedCity}\" not found. Check the spelling and try again."
            401  -> "Invalid API key. Please check your configuration."
            429  -> "Too many requests. Please wait a moment and try again."
            500, 502, 503 -> "Weather service is temporarily unavailable. Try again shortly."
            else -> "Server error (${e.code()}). Please try again."
        }
        is UnknownHostException -> "No internet connection. Please check your network."
        is IOException          -> "Network error. Please check your connection and try again."
        else                    -> e.message?.take(120) ?: "An unexpected error occurred."
    }
}