package com.andb.apps.weather

import com.andb.apps.weather.objects.LocalDateTimeAdapter
import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

object DarkSkyRepo {

    const val API_KEY = "70a330de067b80d8c9b877f4314068f0"

    private val moshi = Moshi.Builder()
        .add(ZoneOffsetAdapter())
        .add(LocalDateTimeAdapter())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.darksky.net/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(DarkSkyService::class.java)

    suspend fun getForecast(lat: Double, long: Double): DarkSkyRequest {
        return service.getForecastExtended(API_KEY, lat, long)
    }

}

interface DarkSkyService {
    @GET("forecast/{apiKey}/{latitude},{longitude}")
    suspend fun getForecast(@Path("apiKey") apiKey: String,
                            @Path("latitude") lat: Double,
                            @Path("longitude") long: Double): DarkSkyRequest

    @GET("forecast/{apiKey}/{latitude},{longitude}?extend=hourly")
    suspend fun getForecastExtended(@Path("apiKey") apiKey: String,
                                    @Path("latitude") lat: Double,
                                    @Path("longitude") long: Double): DarkSkyRequest
}