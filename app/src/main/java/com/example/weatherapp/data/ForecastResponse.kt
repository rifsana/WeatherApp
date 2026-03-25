package com.example.weatherapp.data

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: ForecastCity
)

data class ForecastItem(
    val dt:      Long,
    val main:    Main,
    val weather: List<Weather>,
    val wind:    Wind,
    val dt_txt:  String          // "2024-01-15 12:00:00"
)

data class ForecastCity(
    val name:    String,
    val country: String,
    val sunrise: Long,
    val sunset:  Long
)