package com.andb.apps.weather.data.remote

import com.andb.apps.weather.data.model.weatherkit.WeatherKitRequest
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path


interface WeatherKitService {
    @GET("api/v1/weather/{language}/{latitude}/{longitude}")
    suspend fun getForecast(
        @Path("language") language: String,
        @Path("latitude") lat: Double,
        @Path("longitude") long: Double,
        @Header("Authorization: Bearer") token: String,
    ): WeatherKitRequest
}