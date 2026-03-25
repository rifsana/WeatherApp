package com.example.weatherapp.data

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather")
    suspend fun getWeatherByCoords(
        @Query("lat")   lat:    Double,
        @Query("lon")   lon:    Double,
        @Query("appid") apiKey: String,
        @Query("units") units:  String
    ): WeatherResponse

    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q")     city:   String,
        @Query("appid") apiKey: String,
        @Query("units") units:  String
    ): WeatherResponse

    @GET("forecast")
    suspend fun getForecastByCoords(
        @Query("lat")   lat:    Double,
        @Query("lon")   lon:    Double,
        @Query("appid") apiKey: String,
        @Query("units") units:  String
    ): ForecastResponse

    @GET("forecast")
    suspend fun getForecastByCity(
        @Query("q")     city:   String,
        @Query("appid") apiKey: String,
        @Query("units") units:  String
    ): ForecastResponse
}