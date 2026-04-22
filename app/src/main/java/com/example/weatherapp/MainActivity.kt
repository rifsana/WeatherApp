// MainActivity.kt
package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.data.RetrofitInstance
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.ui.WeatherScreen
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.viewmodel.ThemeMode
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.example.weatherapp.viewmodel.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = remember {
                WeatherRepository(api = RetrofitInstance.api, apiKey = "2d264bcdc410fe844deaeb57155a7b35")
            }
            val vm: WeatherViewModel = viewModel(factory = WeatherViewModelFactory(repository))
            val settings by vm.settings.collectAsState()

            val isSystemDark = isSystemInDarkTheme()
            val useDarkTheme = when (settings.themeMode) {
                ThemeMode.Dark   -> true
                ThemeMode.Light  -> false
                ThemeMode.System -> isSystemDark
            }

            WeatherAppTheme(darkTheme = useDarkTheme) {
                WeatherScreen(vm = vm)
            }
        }
    }
}