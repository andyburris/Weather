package com.andb.apps.weather.data.model

import com.andb.apps.weather.util.secondsToLocalDateTime
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

data class Conditions(
    val current: CurrentConditions,
    val minutely: Minutely,
    val days: List<DayItem>
)

data class DailyConditions(
    val date: LocalDate,
    val summary: String,
    val icon: WeatherIcon,
    val sunriseTime: ZonedDateTime,
    val sunsetTime: ZonedDateTime,
    val moonPhase: MoonPhase,
    val precipIntensity: Double,
    val precipIntensityMax: Double,
    val precipProbability: Double,
    val precipType: PrecipitationType,
    val temperatureHigh: Double,
    val temperatureLow: Double,
    val apparentTemperatureHigh: Double,
    val apparentTemperatureLow: Double,
    val humidity: Double,
    val pressure: Double,
    val windSpeed: Double,
    val cloudCover: Double,
    val uvIndex: Int,
    val visibility: Double,
    val ozone: Double
)

data class HourlyConditions(
    val time: ZonedDateTime,
    val summary: String,
    val icon: WeatherIcon,
    val precipIntensity: Double,
    val precipProbability: Double,
    val precipType: PrecipitationType,
    val temperature: Double,
    val apparentTemperature: Double,
    val dewPoint: Double,
    val humidity: Double,
    val pressure: Double,
    val windSpeed: Double,
    val windGust: Double,
    val windDirection: Int,
    val cloudCover: Double,
    val uvIndex: Int,
    val visibility: Double,
    val ozone: Double
)

data class MinutelyConditions(
    val time: ZonedDateTime,
    val precipIntensity: Double,
    val precipType: PrecipitationType
)

data class CurrentConditions(
    val time: ZonedDateTime,
    val summary: String,
    val icon: WeatherIcon,
    val precipIntensity: Double,
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Double,
    val pressure: Double,
    val windSpeed: Double,
    val windGust: Double,
    val windDirection: Int,
    val cloudCover: Double,
    val uvIndex: Int,
    val visibility: Double,
    val ozone: Double
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