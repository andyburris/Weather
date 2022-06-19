package com.andb.apps.weather.data.remote

import com.andb.apps.weather.data.model.darksky.DarkSkyRequest
import retrofit2.http.GET
import retrofit2.http.Path


interface DarkSkyService {
    @GET("forecast/{apiKey}/{latitude},{longitude}")
    suspend fun getForecast(
        @Path("apiKey") apiKey: String,
        @Path("latitude") lat: Double,
        @Path("longitude") long: Double
    ): DarkSkyRequest

    @GET("forecast/{apiKey}/{latitude},{longitude}?extend=hourly")
    suspend fun getForecastExtended(
        @Path("apiKey") apiKey: String,
        @Path("latitude") lat: Double,
        @Path("longitude") long: Double
    ): DarkSkyRequest
}