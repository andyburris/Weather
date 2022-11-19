package com.andb.apps.weather.data.model.weatherkit

import com.squareup.moshi.Json
import java.time.LocalDateTime
import java.time.ZoneOffset

data class WeatherKitRequest(
    @field:Json(name = "latitude") val latitude: Float,
    @field:Json(name = "longitude") val longitude: Float,
    @field:Json(name = "timezone") val timezone: ZoneOffset,
    @field:Json(name = "currently") val currently: WeatherKitCurrentConditions,
    @field:Json(name = "minutely") val minutely: WeatherKitMinutely,
    @field:Json(name = "hourly") val hourly: WeatherKitHourly,
    @field:Json(name = "daily") val daily: WeatherKitDaily
)

data class WeatherKitCurrentConditions(
    @field:Json(name = "time") val time: LocalDateTime,
    @field:Json(name = "summary") val summary: String,
    @field:Json(name = "icon") val icon: String,
    @field:Json(name = "nearestStormDistance") val nearestStormDistance: Int,
    @field:Json(name = "nearestStormBearing") val nearestStormBearing: Int?,
    @field:Json(name = "precipIntensity") val precipIntensity: Double,
    @field:Json(name = "precipProbability") val precipProbability: Double,
    @field:Json(name = "temperature") val temperature: Double,
    @field:Json(name = "apparentTemperature") val apparentTemperature: Double,
    @field:Json(name = "dewPoint") val dewPoint: Double,
    @field:Json(name = "humidity") val humidity: Double,
    @field:Json(name = "pressure") val pressure: Double,
    @field:Json(name = "windSpeed") val windSpeed: Double,
    @field:Json(name = "windGust") val windGust: Double,
    @field:Json(name = "windBearing") val windBearing: Int,
    @field:Json(name = "cloudCover") val cloudCover: Double,
    @field:Json(name = "uvIndex") val uvIndex: Int,
    @field:Json(name = "visibility") val visibility: Double,
    @field:Json(name = "ozone") val ozone: Double
)

data class WeatherKitMinutely(
    val summary: String,
    val icon: String,
    val data: List<WeatherKitMinutelyConditions>
)

data class WeatherKitMinutelyConditions(
    @field:Json(name = "time") val time: LocalDateTime,
    @field:Json(name = "precipIntensity") val precipIntensity: Double,
    @field:Json(name = "precipIntensityError") val precipIntensityError: Double?,
    @field:Json(name = "precipProbability") val precipProbability: Double,
    @field:Json(name = "precipType") val precipType: String?
)

class WeatherKitHourly(
    val summary: String,
    val icon: String,
    val data: List<WeatherKitHourlyConditions>
)

data class WeatherKitHourlyConditions(
    @field:Json(name = "time") val time: LocalDateTime,
    @field:Json(name = "summary") val summary: String,
    @field:Json(name = "icon") val icon: String,
    @field:Json(name = "precipIntensity") val precipIntensity: Double,
    @field:Json(name = "precipProbability") val precipProbability: Double,
    @field:Json(name = "precipType") val precipType: String?,
    @field:Json(name = "temperature") val temperature: Double,
    @field:Json(name = "apparentTemperature") val apparentTemperature: Double,
    @field:Json(name = "dewPoint") val dewPoint: Double,
    @field:Json(name = "humidity") val humidity: Double,
    @field:Json(name = "pressure") val pressure: Double,
    @field:Json(name = "windSpeed") val windSpeed: Double,
    @field:Json(name = "windGust") val windGust: Double,
    @field:Json(name = "windBearing") val windBearing: Int,
    @field:Json(name = "cloudCover") val cloudCover: Double,
    @field:Json(name = "uvIndex") val uvIndex: Int,
    @field:Json(name = "visibility") val visibility: Double,
    @field:Json(name = "ozone") val ozone: Double
)

data class WeatherKitDaily(
    val summary: String,
    val icon: String,
    val data: List<WeatherKitDailyConditions>
)

data class WeatherKitDailyConditions(
    @field:Json(name = "time") val time: LocalDateTime,
    @field:Json(name = "summary") val summary: String,
    @field:Json(name = "icon") val icon: String,
    @field:Json(name = "sunriseTime") val sunriseTime: LocalDateTime,
    @field:Json(name = "sunsetTime") val sunsetTime: LocalDateTime,
    @field:Json(name = "moonPhase") val moonPhase: Double,
    @field:Json(name = "precipIntensity") val precipIntensity: Double,
    @field:Json(name = "precipIntensityMax") val precipIntensityMax: Double,
    @field:Json(name = "precipIntensityMaxTime") val precipIntensityMaxTime: LocalDateTime?,
    @field:Json(name = "precipProbability") val precipProbability: Double,
    @field:Json(name = "precipType") val precipType: String?,
    @field:Json(name = "temperatureHigh") val temperatureHigh: Double,
    @field:Json(name = "temperatureHighTime") val temperatureHighTime: LocalDateTime,
    @field:Json(name = "temperatureLow") val temperatureLow: Double,
    @field:Json(name = "temperatureLowTime") val temperatureLowTime: LocalDateTime,
    @field:Json(name = "apparentTemperatureHigh") val apparentTemperatureHigh: Double,
    @field:Json(name = "apparentTemperatureHighTime") val apparentTemperatureHighTime: LocalDateTime,
    @field:Json(name = "apparentTemperatureLow") val apparentTemperatureLow: Double,
    @field:Json(name = "apparentTemperatureLowTime") val apparentTemperatureLowTime: LocalDateTime,
    @field:Json(name = "dewPoint") val dewPoint: Double,
    @field:Json(name = "humidity") val humidity: Double,
    @field:Json(name = "pressure") val pressure: Double,
    @field:Json(name = "windSpeed") val windSpeed: Double,
    @field:Json(name = "windGust") val windGust: Double,
    @field:Json(name = "windGustTime") val windGustTime: LocalDateTime,
    @field:Json(name = "windBearing") val windBearing: Int,
    @field:Json(name = "cloudCover") val cloudCover: Double,
    @field:Json(name = "uvIndex") val uvIndex: Int,
    @field:Json(name = "uvIndexTime") val uvIndexTime: LocalDateTime,
    @field:Json(name = "visibility") val visibility: Double,
    @field:Json(name = "ozone") val ozone: Double,
    @field:Json(name = "temperatureMin") val temperatureMin: Double,
    @field:Json(name = "temperatureMinTime") val temperatureMinTime: LocalDateTime,
    @field:Json(name = "temperatureMax") val temperatureMax: Double,
    @field:Json(name = "temperatureMaxTime") val temperatureMaxTime: LocalDateTime,
    @field:Json(name = "apparentTemperatureMin") val apparentTemperatureMin: Double,
    @field:Json(name = "apparentTemperatureMinTime") val apparentTemperatureMinTime: LocalDateTime,
    @field:Json(name = "apparentTemperatureMax") val apparentTemperatureMax: Double,
    @field:Json(name = "apparentTemperatureMaxTime") val apparentTemperatureMaxTime: LocalDateTime
)