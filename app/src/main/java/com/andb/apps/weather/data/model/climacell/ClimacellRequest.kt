package com.andb.apps.weather.data.model.climacell

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

data class ClimacellRequest(
    val current: ClimacellConditions,
    val nowcast: List<ClimacellConditions>,
    val hourly: List<ClimacellHourlyForecast>,
    val daily: List<ClimacellDailyForecast>
)

data class ClimacellConditions(
    val lat: Double,
    @Json(name = "lon") val long: Double,
    val temp: ClimacellValue<Double>,
    @Json(name = "feels_like") val feelsLike: ClimacellValue<Double>,
    @Json(name = "wind_speed") val windSpeed: ClimacellValue<Double>,
    @Json(name = "wind_gust") val windGust: ClimacellValue<Double>,
    @Json(name = "wind_direction") val windDirection: ClimacellValue<Double>,
    val visibility: ClimacellValue<Double>,
    val precipitation: ClimacellValueNullable<Double>,
    @Json(name = "precipitation_type") val precipitationType: ClimacellValueNullable<String>,
    val humidity: ClimacellValue<Double>,
    @Json(name = "baro_pressure") val baroPressure: ClimacellValue<Double>,
    @Json(name = "cloud_cover") val cloudCover: ClimacellValue<Double>,
    @Json(name = "o3") val ozone: ClimacellValueNullable<Double>,
    @Json(name = "weather_code") val weatherCode: ClimacellValue<String>,
    @Json(name = "observation_time") val observationTime: ClimacellValue<ZonedDateTime>
)

data class ClimacellHourlyForecast(
    val lat: Double,
    @Json(name = "lon") val long: Double,
    val temp: ClimacellValue<Double>,
    @Json(name = "feels_like") val feelsLike: ClimacellValue<Double>,
    @Json(name = "wind_speed") val windSpeed: ClimacellValue<Double>,
    @Json(name = "wind_gust") val windGust: ClimacellValue<Double>,
    @Json(name = "wind_direction") val windDirection: ClimacellValue<ClimacellWindDirection>,
    val visibility: ClimacellValue<Double>,
    val precipitation: ClimacellValue<Double>,
    @Json(name = "precipitation_type") val precipitationType: ClimacellValue<String>,
    @Json(name = "precipitation_probability") val precipitationProbability: ClimacellValue<Int>,
    val humidity: ClimacellValue<Double>,
    @Json(name = "baro_pressure") val baroPressure: ClimacellValue<Double>,
    @Json(name = "cloud_cover") val cloudCover: ClimacellValue<Double>,
    @Json(name = "o3") val ozone: ClimacellValueNullable<Double>,
    @Json(name = "weather_code") val weatherCode: ClimacellValue<String>,
    @Json(name = "observation_time") val observationTime: ClimacellValue<ZonedDateTime>
)

data class ClimacellDailyForecast(
    val lat: Double,
    @Json(name = "lon") val long: Double,
    val temp: List<ClimacellDailyValue<Double>>,
    val precipitation: List<ClimacellDailyValue<Double>>,
    @Json(name = "precipitation_probability") val precipitationProbability: ClimacellValue<Int>,
    @Json(name = "feels_like") val feelsLike: List<ClimacellDailyValue<Double>>,
    val humidity: List<ClimacellDailyValue<Double>>,
    @Json(name = "baro_pressure") val baroPressure: List<ClimacellDailyValue<Double>>,
    @Json(name = "wind_speed") val windSpeed: List<ClimacellDailyValue<Double>>,
    val visibility: List<ClimacellDailyValue<Double>>,
    val sunrise: ClimacellValue<ZonedDateTime>,
    val sunset: ClimacellValue<ZonedDateTime>,
    @Json(name = "moon_phase") val moonPhase: ClimacellValue<String>,
    @Json(name = "weather_code") val weatherCode: ClimacellValue<String>,
    @Json(name = "observation_time") val observationTime: ClimacellValue<LocalDate>
)

data class ClimacellDailyValue<T>(
    @Json(name = "observation_time") val observationTime: ZonedDateTime,
    val min: ClimacellValue<T>?,
    val max: ClimacellValue<T>?
)


class ClimacellValue<T>(override val value: T, units: String = "") :
    ClimacellValueNullable<T>(value, units)

open class ClimacellValueNullable<T>(open val value: T?, val units: String = "")
data class ClimacellWindDirection(val direction: String) {
    fun toDirection() = when (direction) {
        "none" -> 0.0
        else -> direction.toDouble()
    }
}

data class ClimacellOzone(val ozone: Int?)

fun <T> List<ClimacellDailyValue<T>>.max() =
    this.find { it.max != null }?.max ?: throw Error("$this does not have a max component")

fun <T> List<ClimacellDailyValue<T>>.min() =
    this.find { it.min != null }?.min ?: throw Error("$this does not have a min component")

fun <T : Number> List<ClimacellDailyValue<T>>.average() =
    (max().value.toDouble() + min().value.toDouble()) / 2

class ZonedDateTimeAdapter {
    @ToJson
    fun toJson(dt: ZonedDateTime): String? {
        return dt.toString()
    }

    @FromJson
    fun fromJson(string: String): ZonedDateTime {
        return ZonedDateTime.parse(string).withZoneSameInstant(ZoneId.systemDefault())
    }
}

class LocalDateAdapter {
    @ToJson
    fun toJson(ld: LocalDate): String? {
        return ld.toString()
    }

    @FromJson
    fun fromJson(string: String): LocalDate {
        return LocalDate.parse(string)
    }
}


class ClimacellWindDirectionAdapter {
    @ToJson
    fun toJson(cwd: ClimacellWindDirection): String? {
        return cwd.toString()
    }

    @FromJson
    fun fromJson(string: String): ClimacellWindDirection {
        return ClimacellWindDirection(string)
    }
}

class ClimacellOzoneAdapter {
    @ToJson
    fun toJson(o3: ClimacellOzone?): String? {
        return o3?.ozone.toString()
    }

    @FromJson
    fun fromJson(string: String?): ClimacellOzone? {
        //Log.d("climacellOzoneAdapter", "deserializing $string")
        return ClimacellOzone(string?.toIntOrNull())
    }
}
