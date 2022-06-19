package com.andb.apps.weather.data.model.darksky

import com.andb.apps.weather.data.model.*
import org.threeten.bp.ZoneOffset

fun DarkSkyRequest.toConditions(): Conditions {
    return Conditions(
        currently.toCurrentConditions(timezone),
        minutely.toMinutely(timezone),
        daily.data.map { day ->
            DayItem(
                day = day.toDailyConditions(timezone),
                hourly = hourly.data
                    .filter { it.time.dayOfMonth == day.time.dayOfMonth }
                    .map { it.toHourlyConditions(timezone) }
            )
        }
    )
}

fun DarkSkyCurrentConditions.toCurrentConditions(timeZone: ZoneOffset): CurrentConditions {
    return CurrentConditions(
        time.atOffset(timeZone).toZonedDateTime(),
        summary,
        icon.toWeatherIcon(),
        precipProbability * 100,
        temperature,
        apparentTemperature,
        humidity,
        pressure,
        windSpeed,
        windGust,
        windBearing + 180,
        cloudCover,
        uvIndex,
        visibility,
        ozone
    )
}

fun DarkSkyMinutely.toMinutely(timeZone: ZoneOffset): Minutely {
    return Minutely(summary, data.map { it.toMinutelyConditions(timeZone) })
}

fun DarkSkyMinutelyConditions.toMinutelyConditions(timeZone: ZoneOffset): MinutelyConditions {
    return MinutelyConditions(
        time.atOffset(timeZone).toZonedDateTime(),
        precipIntensity,
        precipType.toPrecipitationType()
    )
}

fun DarkSkyHourlyConditions.toHourlyConditions(timeZone: ZoneOffset): HourlyConditions {
    return HourlyConditions(
        time.atOffset(timeZone).toZonedDateTime(),
        summary,
        icon.toWeatherIcon(),
        precipIntensity,
        precipProbability,
        precipType.toPrecipitationType(),
        temperature,
        apparentTemperature,
        dewPoint,
        humidity,
        pressure,
        windSpeed,
        windGust,
        windBearing + 180,
        cloudCover,
        uvIndex,
        visibility,
        ozone
    )
}

fun DarkSkyDailyConditions.toDailyConditions(timeZone: ZoneOffset): DailyConditions {
    return DailyConditions(
        time.atOffset(timeZone).toLocalDate(),
        summary,
        icon.toWeatherIcon(),
        sunriseTime.atOffset(timeZone).toZonedDateTime(),
        sunsetTime.atOffset(timeZone).toZonedDateTime(),
        moonPhase.toMoonPhase(),
        precipIntensity,
        precipIntensityMax,
        precipProbability,
        precipType.toPrecipitationType(),
        temperatureHigh,
        temperatureLow,
        apparentTemperatureHigh,
        apparentTemperatureLow,
        humidity,
        pressure,
        windSpeed,
        cloudCover,
        uvIndex,
        visibility,
        ozone
    )
}

private fun String.toWeatherIcon(): WeatherIcon {
    return when (this) {
        "clear-day" -> WeatherIcon.CLEAR
        "clear-night" -> WeatherIcon.CLEAR
        "rain" -> WeatherIcon.RAIN
        "snow" -> WeatherIcon.SNOW
        "sleet" -> WeatherIcon.SLEET
        "wind" -> WeatherIcon.WIND
        "fog" -> WeatherIcon.FOG
        "cloudy" -> WeatherIcon.CLOUDY
        "partly-cloudy-day" -> WeatherIcon.PARTLY_CLOUDY
        "partly-cloudy-night" -> WeatherIcon.PARTLY_CLOUDY
        "none" -> WeatherIcon.NONE
        else -> WeatherIcon.NONE
    }
}

private fun Double.toMoonPhase(): MoonPhase {
    return when (this) {
        0.0 -> MoonPhase.NEW_MOON
        0.25 -> MoonPhase.FIRST_QUARTER
        0.5 -> MoonPhase.FULL
        0.75 -> MoonPhase.THIRD_QUARTER
        in 0.0..0.25 -> MoonPhase.WAXING_CRESCENT
        in 0.25..0.50 -> MoonPhase.WAXING_GIBBOUS
        in 0.50..0.75 -> MoonPhase.WANING_GIBBOUS
        in 0.75..1.00 -> MoonPhase.WANING_CRESCENT
        else -> MoonPhase.NEW_MOON
    }
}

private fun String?.toPrecipitationType(): PrecipitationType {
    return when (this) {
        "rain" -> PrecipitationType.RAIN
        "snow" -> PrecipitationType.SNOW
        "sleet" -> PrecipitationType.SLEET
        else -> PrecipitationType.NONE
    }
}