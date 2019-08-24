package com.andb.apps.weather.objects

import com.andb.apps.weather.secondsToLocalDateTime
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

data class DailyConditions(
    @field:Json(name = "time") val time: LocalDateTime,
    @field:Json(name = "summary") val summary: String,
    @field:Json(name = "icon") val icon: String,
    @field:Json(name = "sunriseTime") val sunriseTime: Int,
    @field:Json(name = "sunsetTime") val sunsetTime: Int,
    @field:Json(name = "moonPhase") val moonPhase: Double,
    @field:Json(name = "precipIntensity") val precipIntensity: Double,
    @field:Json(name = "precipIntensityMax") val precipIntensityMax: Double,
    @field:Json(name = "precipIntensityMaxTime") val precipIntensityMaxTime: Int,
    @field:Json(name = "precipProbability") val precipProbability: Double,
    @field:Json(name = "precipType") val precipType: String,
    @field:Json(name = "temperatureHigh") val temperatureHigh: Double,
    @field:Json(name = "temperatureHighTime") val temperatureHighTime: Int,
    @field:Json(name = "temperatureLow") val temperatureLow: Double,
    @field:Json(name = "temperatureLowTime") val temperatureLowTime: Int,
    @field:Json(name = "apparentTemperatureHigh") val apparentTemperatureHigh: Double,
    @field:Json(name = "apparentTemperatureHighTime") val apparentTemperatureHighTime: Int,
    @field:Json(name = "apparentTemperatureLow") val apparentTemperatureLow: Double,
    @field:Json(name = "apparentTemperatureLowTime") val apparentTemperatureLowTime: Int,
    @field:Json(name = "dewPoint") val dewPoint: Double,
    @field:Json(name = "humidity") val humidity: Double,
    @field:Json(name = "pressure") val pressure: Double,
    @field:Json(name = "windSpeed") val windSpeed: Double,
    @field:Json(name = "windGust") val windGust: Double,
    @field:Json(name = "windGustTime") val windGustTime: Int,
    @field:Json(name = "windBearing") val windBearing: Int,
    @field:Json(name = "cloudCover") val cloudCover: Double,
    @field:Json(name = "uvIndex") val uvIndex: Int,
    @field:Json(name = "uvIndexTime") val uvIndexTime: Int,
    @field:Json(name = "visibility") val visibility: Double,
    @field:Json(name = "ozone") val ozone: Double,
    @field:Json(name = "temperatureMin") val temperatureMin: Double,
    @field:Json(name = "temperatureMinTime") val temperatureMinTime: Int,
    @field:Json(name = "temperatureMax") val temperatureMax: Double,
    @field:Json(name = "temperatureMaxTime") val temperatureMaxTime: Int,
    @field:Json(name = "apparentTemperatureMin") val apparentTemperatureMin: Double,
    @field:Json(name = "apparentTemperatureMinTime") val apparentTemperatureMinTime: Int,
    @field:Json(name = "apparentTemperatureMax") val apparentTemperatureMax: Double,
    @field:Json(name = "apparentTemperatureMaxTime") val apparentTemperatureMaxTime: Int
)

data class HourlyConditions(
    @field:Json(name = "time") val time: LocalDateTime,
    @field:Json(name = "summary") val summary: String,
    @field:Json(name = "icon") val icon: String,
    @field:Json(name = "precipIntensity") val precipIntensity: Double,
    @field:Json(name = "precipProbability") val precipProbability: Double,
    @field:Json(name = "precipType") val precipType: String,
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

data class MinutelyConditions(
    @field:Json(name = "time") val time: LocalDateTime,
    @field:Json(name = "precipIntensity") val precipIntensity: Double,
    @field:Json(name = "precipIntensityError") val precipIntensityError: Double,
    @field:Json(name = "precipProbability") val precipProbability: Double,
    @field:Json(name = "precipType") val precipType: String
)

data class CurrentConditions(
    @field:Json(name = "time") val time: LocalDateTime,
    @field:Json(name = "summary") val summary: String,
    @field:Json(name = "icon") val icon: String,
    @field:Json(name = "nearestStormDistance") val nearestStormDistance: Int,
    @field:Json(name = "nearestStormBearing") val nearestStormBearing: Int,
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

class LocalDateTimeAdapter {
    @ToJson
    fun toJson(dt: LocalDateTime): String? {
        return dt.toEpochSecond(ZoneOffset.UTC).toString()
    }

    @FromJson
    fun fromJson(string: String): LocalDateTime {
        return secondsToLocalDateTime(string.toLong())
    }
}