package com.andb.apps.weather.data.remote

import com.andb.apps.weather.data.model.weatherkit.WeatherKitRequest
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


interface WeatherKitService {
    @GET("api/v1/weather/{language}/{latitude}/{longitude}")
    suspend fun getForecast(
        @Path("language") language: String,
        @Path("latitude") lat: Double,
        @Path("longitude") long: Double,
        @Query("dataSets") dataSets: String,
        @Header("Authorization") token: String,
    ): WeatherKitRequest
}

enum class DataSets {
    /**The current weather for the requested location.**/
    currentWeather,

    /**The daily forecast for the requested location.**/
    forecastDaily,

    /**The hourly forecast for the requested location.**/
    forecastHourly,

    /**The next hour forecast for the requested location.**/
    forecastNextHour,

    /**Weather alerts for the requested location.**/
    weatherAlerts,
}