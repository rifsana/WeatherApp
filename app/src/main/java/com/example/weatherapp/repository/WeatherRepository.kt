package com.example.weatherapp.repository

import com.example.weatherapp.data.ForecastResponse
import com.example.weatherapp.data.WeatherApiService
import com.example.weatherapp.data.WeatherResponse

class WeatherRepository(
    private val api:    WeatherApiService,
    private val apiKey: String
) {
    suspend fun getWeatherByCoords(lat: Double, lon: Double): WeatherResponse =
        api.getWeatherByCoords(lat, lon, apiKey, "metric")

    suspend fun getForecastByCoords(lat: Double, lon: Double): ForecastResponse =
        api.getForecastByCoords(lat, lon, apiKey, "metric")

    suspend fun getWeatherByCity(city: String): WeatherResponse =
        api.getWeatherByCity(city.trim(), apiKey, "metric")

    suspend fun getForecastByCity(city: String): ForecastResponse =
        api.getForecastByCity(city.trim(), apiKey, "metric")
}