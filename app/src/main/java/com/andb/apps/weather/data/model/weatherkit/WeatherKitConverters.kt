package com.andb.apps.weather.data.model.weatherkit

import com.andb.apps.weather.data.model.ConditionCode
import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.data.model.CurrentConditions
import com.andb.apps.weather.data.model.DailyConditions
import com.andb.apps.weather.data.model.DayItem
import com.andb.apps.weather.data.model.HourlyConditions
import com.andb.apps.weather.data.model.Minutely
import com.andb.apps.weather.data.model.MinutelyConditions
import com.andb.apps.weather.data.model.MoonPhase
import com.andb.apps.weather.data.model.PrecipitationType
import com.andb.apps.weather.data.toFahrenheit
import com.andb.apps.weather.data.toMph
import java.time.ZonedDateTime

fun WeatherKitRequest.toConditions(): Conditions {
    val allHourlyConditions = this.forecastHourly.hours.map { it.toHourlyConditions() }
    return Conditions(
        currentWeather.toCurrentConditions(),
        forecastNextHour.toMinutely(),
        forecastDaily.days.map { day ->
            val dayOfMonth = ZonedDateTime.parse(day.forecastStart).toLocalDate().dayOfMonth
            DayItem(
                day = day.toDailyConditions(allHourlyConditions.filter { it.time.dayOfMonth == dayOfMonth }),
                hourly = allHourlyConditions.filter { it.time.dayOfMonth == dayOfMonth }
            )
        }
    )
}

fun WeatherKitCurrentConditions.toCurrentConditions(): CurrentConditions {
    return CurrentConditions(
        ZonedDateTime.parse(asOf),
        "",
        conditionCode.toWeatherIcon(),
        precipitationIntensity,
        temperature.toFahrenheit(),
        temperatureApparent.toFahrenheit(),
        humidity,
        pressure,
        windSpeed.toMph(),
        windGust.toMph(),
        windDirection,
        cloudCover,
        uvIndex,
        visibility,
    )
}

fun WeatherKitNextHour.toMinutely(): Minutely {
    return Minutely(
        generateSummary(),
        minutes.map { it.toMinutelyConditions(summary.first().condition) })
}

private val precipIntensityThreshold = 0.1
fun WeatherKitNextHour.generateSummary(): String {
    println("generating next hour summary, summary = ${this.summary}")

    return when {
        summary.isEmpty() -> throw Error("Summary should always have a value")
        summary.size == 1 -> "${summary.first().condition.name.capitalize()} for the next hour"
        summary.size == 2 -> "${summary[0].condition.name.capitalize()} for ${
            ZonedDateTime.parse(
                summary[1].startTime
            ).minute - ZonedDateTime.now().minute
        } minutes, then ${summary[1].condition.name} for the rest of the hour"

        else -> "${summary[0].condition.name.capitalize()} for ${ZonedDateTime.parse(summary[1].startTime).minute - ZonedDateTime.now().minute} minutes, then ${summary[1].condition.name} for ${
            ZonedDateTime.parse(
                summary[2].startTime
            ).minute - ZonedDateTime.parse(summary[1].startTime).minute
        } minutes"
    }
}

fun WeatherKitMinuteConditions.toMinutelyConditions(precipitationType: WeatherKitPrecipitationType): MinutelyConditions {
    return MinutelyConditions(
        ZonedDateTime.parse(startTime),
        precipitationChance,
        precipitationIntensity,
        precipitationType.toPrecipitationType()
    )
}

fun WeatherKitHourlyConditions.toHourlyConditions(): HourlyConditions {
    return HourlyConditions(
        ZonedDateTime.parse(forecastStart),
        "",
        conditionCode.toWeatherIcon(),
        precipitationIntensity,
        precipitationChance,
        precipitationType.toPrecipitationType(),
        temperature.toFahrenheit(),
        temperatureApparent.toFahrenheit(),
        humidity,
        pressure,
        windSpeed.toMph(),
        windGust.toMph(),
        windDirection,
        cloudCover,
        uvIndex,
        visibility,
    )
}

fun WeatherKitDailyConditions.toDailyConditions(hours: List<HourlyConditions>): DailyConditions {
    return DailyConditions(
        ZonedDateTime.parse(forecastStart).toLocalDate(),
        "",
        conditionCode.toWeatherIcon(),
        ZonedDateTime.parse(sunrise),
        ZonedDateTime.parse(sunset),
        moonPhase.toMoonPhase(),
        precipitationAmount,
        precipIntensityMax = hours.maxOf { it.precipIntensity },
        precipitationChance,
        precipitationType.toPrecipitationType(),
        temperatureMax.toFahrenheit(),
        temperatureMin.toFahrenheit(),
        hours.maxOf { it.apparentTemperature }.toFahrenheit(),
        hours.minOf { it.apparentTemperature }.toFahrenheit(),
        humidity = hours.maxOf { it.humidity },
        pressure = hours.maxOf { it.pressure },
        windSpeed = hours.maxOf { it.windSpeed.toMph() },
        cloudCover = hours.maxOf { it.cloudCover },
        uvIndex = hours.maxOf { it.uvIndex },
        visibility = hours.maxOf { it.visibility },
    )
}

private fun WeatherKitConditionCode.toWeatherIcon(): ConditionCode = when (this) {
    WeatherKitConditionCode.Clear -> ConditionCode.CLEAR
    WeatherKitConditionCode.Cloudy -> ConditionCode.CLOUDY
    WeatherKitConditionCode.Dust -> ConditionCode.FOG
    WeatherKitConditionCode.Fog -> ConditionCode.FOG
    WeatherKitConditionCode.Haze -> ConditionCode.FOG
    WeatherKitConditionCode.MostlyClear -> ConditionCode.PARTLY_CLOUDY
    WeatherKitConditionCode.MostlyCloudy -> ConditionCode.PARTLY_CLOUDY
    WeatherKitConditionCode.PartlyCloudy -> ConditionCode.PARTLY_CLOUDY
    WeatherKitConditionCode.ScatteredThunderstorms -> ConditionCode.THUNDERSTORM
    WeatherKitConditionCode.Smoke -> ConditionCode.FOG
    WeatherKitConditionCode.Breezy -> ConditionCode.WIND
    WeatherKitConditionCode.Windy -> ConditionCode.WIND
    WeatherKitConditionCode.Drizzle -> ConditionCode.RAIN
    WeatherKitConditionCode.HeavyRain -> ConditionCode.RAIN
    WeatherKitConditionCode.Rain -> ConditionCode.RAIN
    WeatherKitConditionCode.Showers -> ConditionCode.RAIN
    WeatherKitConditionCode.Flurries -> ConditionCode.SNOW
    WeatherKitConditionCode.HeavySnow -> ConditionCode.SNOW
    WeatherKitConditionCode.MixedRainAndSleet -> ConditionCode.SLEET
    WeatherKitConditionCode.MixedRainAndSnow -> ConditionCode.SLEET
    WeatherKitConditionCode.MixedRainfall -> ConditionCode.SLEET
    WeatherKitConditionCode.MixedSnowAndSleet -> ConditionCode.SLEET
    WeatherKitConditionCode.ScatteredShowers -> ConditionCode.RAIN
    WeatherKitConditionCode.ScatteredSnowShowers -> ConditionCode.SNOW
    WeatherKitConditionCode.Sleet -> ConditionCode.SLEET
    WeatherKitConditionCode.Snow -> ConditionCode.SNOW
    WeatherKitConditionCode.SnowShowers -> ConditionCode.SNOW
    WeatherKitConditionCode.Blizzard -> ConditionCode.SNOW
    WeatherKitConditionCode.BlowingSnow -> ConditionCode.SNOW
    WeatherKitConditionCode.FreezingDrizzle -> ConditionCode.HAIL
    WeatherKitConditionCode.FreezingRain -> ConditionCode.HAIL
    WeatherKitConditionCode.Frigid -> ConditionCode.SNOW
    WeatherKitConditionCode.Hail -> ConditionCode.HAIL
    WeatherKitConditionCode.Hot -> ConditionCode.CLEAR
    WeatherKitConditionCode.Hurricane -> ConditionCode.THUNDERSTORM
    WeatherKitConditionCode.IsolatedThunderstorms -> ConditionCode.THUNDERSTORM
    WeatherKitConditionCode.Thunderstorms -> ConditionCode.THUNDERSTORM
    WeatherKitConditionCode.Tornado -> ConditionCode.WIND
    WeatherKitConditionCode.TropicalStorm -> ConditionCode.THUNDERSTORM
}

private fun WeatherKitMoonPhase.toMoonPhase(): MoonPhase {
    return when (this) {
        WeatherKitMoonPhase.new -> MoonPhase.NEW_MOON
        WeatherKitMoonPhase.firstQuarter -> MoonPhase.FIRST_QUARTER
        WeatherKitMoonPhase.full -> MoonPhase.FULL
        WeatherKitMoonPhase.thirdQuarter -> MoonPhase.THIRD_QUARTER
        WeatherKitMoonPhase.waxingCrescent -> MoonPhase.WAXING_CRESCENT
        WeatherKitMoonPhase.waxingGibbous -> MoonPhase.WAXING_GIBBOUS
        WeatherKitMoonPhase.waningGibbous -> MoonPhase.WANING_GIBBOUS
        WeatherKitMoonPhase.waningCrescent -> MoonPhase.WANING_CRESCENT
    }
}

private fun WeatherKitPrecipitationType.toPrecipitationType() = when (this) {
    WeatherKitPrecipitationType.clear -> PrecipitationType.None
    WeatherKitPrecipitationType.precipitation -> PrecipitationType.Unknown
    WeatherKitPrecipitationType.rain -> PrecipitationType.Rain
    WeatherKitPrecipitationType.snow -> PrecipitationType.Snow
    WeatherKitPrecipitationType.sleet -> PrecipitationType.Sleet
    WeatherKitPrecipitationType.hail -> PrecipitationType.Hail
    WeatherKitPrecipitationType.mixed -> PrecipitationType.Mixed
}