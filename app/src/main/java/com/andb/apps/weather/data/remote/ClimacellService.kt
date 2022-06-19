package com.andb.apps.weather.data.remote

import com.andb.apps.weather.data.model.UnitType
import com.andb.apps.weather.data.model.climacell.ClimacellConditions
import com.andb.apps.weather.data.model.climacell.ClimacellDailyForecast
import com.andb.apps.weather.data.model.climacell.ClimacellHourlyForecast
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface ClimacellService {
    @Headers("accept: application/json")
    @GET("weather/realtime?fields=temp,feels_like,wind_speed,wind_gust,wind_direction,visibility,precipitation,precipitation_type,humidity,baro_pressure,cloud_cover,o3,weather_code")
    suspend fun getCurrent(
        @Header("apikey") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("unit_system") units: UnitType
    ): ClimacellConditions

    @Headers("accept: application/json")
    @GET("weather/nowcast?timestep=1&start_time=now&fields=temp,feels_like,wind_speed,wind_gust,wind_direction,visibility,precipitation,precipitation_type,humidity,baro_pressure,cloud_cover,o3,weather_code")
    suspend fun getNowcast(
        @Header("apikey") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("unit_system") units: UnitType,
        @Query("end_time") endTime: String
    ): List<ClimacellConditions>

    @Headers("accept: application/json")
    @GET("weather/forecast/hourly?start_time=now&fields=temp,feels_like,wind_speed,wind_gust,wind_direction,visibility,precipitation,precipitation_type,precipitation_probability,humidity,baro_pressure,cloud_cover,o3,weather_code")
    suspend fun getHourly(
        @Header("apikey") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("unit_system") units: UnitType
    ): List<ClimacellHourlyForecast>

    @Headers("accept: application/json")
    @GET("weather/forecast/daily?start_time=now&&fields=temp,precipitation,precipitation_probability,feels_like,humidity,baro_pressure,wind_speed,visibility,sunrise,sunset,moon_phase,weather_code")
    suspend fun getDaily(
        @Header("apikey") apiKey: String,
        @Query("lat") lat: Double,
        @Query("lon") long: Double,
        @Query("unit_system") units: UnitType
    ): List<ClimacellDailyForecast>
}