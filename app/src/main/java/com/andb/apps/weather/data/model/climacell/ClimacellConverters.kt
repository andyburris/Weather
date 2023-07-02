package com.andb.apps.weather.data.model.climacell

import com.andb.apps.weather.data.local.Prefs
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

fun ClimacellRequest.toConditions(): Conditions {
    return Conditions(
        current.toCurrentConditions(),
        Minutely(generateNowcastSummary(nowcast), nowcast.map { it.toMinutelyConditions() }),
        daily
            .filter { day ->
                hourly.any { it.observationTime.value.dayOfMonth == day.observationTime.value.dayOfMonth && it.observationTime.value.hour > Prefs.dayStart }
            }.map { day ->
                DayItem(
                    day = day.toDailyConditions(),
                    hourly = hourly
                        .filter { it.observationTime.value.dayOfMonth == day.observationTime.value.dayOfMonth }
                        .map { it.toHourlyConditions() }
                )
            }
    )
}

fun ClimacellConditions.toCurrentConditions(): CurrentConditions {
    return CurrentConditions(
        observationTime.value,
        "",
        weatherCode.value.toWeatherIcon(),
        precipitation.value ?: 0.0,
        temp.value,
        feelsLike.value,
        humidity.value / 100,
        baroPressure.value,
        windSpeed.value,
        windGust.value,
        -windDirection.value.toInt() % 360,
        cloudCover.value.toDouble(),
        -1,
        visibility.value,
    )
}

fun ClimacellConditions.toMinutelyConditions(): MinutelyConditions {
    val precipAmount = precipitation.value ?: 0.0
    return MinutelyConditions(
        observationTime.value,
        if (precipAmount > 0.01) 1.0 else 0.0,
        precipAmount,
        precipitationType.value.toPrecipitationType()
    )
}

fun ClimacellHourlyForecast.toHourlyConditions(): HourlyConditions {
    return HourlyConditions(
        observationTime.value,
        "",
        weatherCode.value.toWeatherIcon(),
        precipitation.value,
        precipitationProbability.value.toDouble() / 100,
        precipitationType.value.toPrecipitationType(),
        temp.value,
        feelsLike.value,
        humidity.value / 100,
        baroPressure.value,
        windSpeed.value,
        windGust.value,
        -windDirection.value.toDirection().toInt() % 360,
        cloudCover.value.toDouble(),
        -1,
        visibility.value,
    )
}

fun ClimacellDailyForecast.toDailyConditions(): DailyConditions {
    return DailyConditions(
        observationTime.value,
        "",
        weatherCode.value.toWeatherIcon(),
        sunrise.value,
        sunset.value,
        moonPhase.value.toMoonPhase(),
        precipitation.max().value,
        precipitation.max().value,
        precipitationProbability.value.toDouble() / 100,
        "".toPrecipitationType(),
        temp.max().value,
        temp.min().value,
        feelsLike.max().value,
        feelsLike.min().value,
        humidity.max().value / 100,
        baroPressure.max().value,
        windSpeed.max().value,
        -1.0,
        -1,
        visibility.average(),
    )
}

fun generateNowcastSummary(nowcastValues: List<ClimacellConditions>): String {
    val changePoints = nowcastValues.filterIndexed { index, climacellConditions ->
        if (index == 0) return@filterIndexed false
        val newRain = (climacellConditions.precipitation.value
            ?: 0.0) > 0.0 && nowcastValues[index - 1].precipitation.value == 0.0
        val endRain =
            climacellConditions.precipitation.value == 0.0 && (nowcastValues[index - 1].precipitation.value
                ?: 0.0) > 0.0
        return@filterIndexed newRain || endRain
    }.mapIndexed { index, climacellConditions ->
        MinutelySummaryPoint(index, climacellConditions.precipitation.value ?: 0.0)
    }

    return generateMinutelySummary(nowcastValues[0].precipitation.value ?: 0.0 > 0.0, changePoints)

}

fun generateMinutelySummary(
    initialRain: Boolean,
    changePoints: List<MinutelySummaryPoint>
): String {
    return when {
        changePoints.isEmpty() && initialRain -> "Rain for the next hour"
        changePoints.isEmpty() -> "No rain"
        changePoints.size > 2 -> "Intermittent rain"
        else -> changePoints.foldIndexed("Rain") { index, acc, minutelySummaryPoint ->
            val separator = if (index == 0) " " else " and "
            val summary = minutelySummaryPoint.summary(changePoints.getOrNull(index - 1))
            return@foldIndexed acc + separator + summary
        }
    }
}

public class MinutelySummaryPoint(val minute: Int, val precipIntensity: Double) {
    fun summary(lastPoint: MinutelySummaryPoint? = null) = when {
        precipIntensity > 0 && lastPoint == null -> "starting in $minute minutes"
        precipIntensity > 0 && lastPoint != null -> "starting ${minute - lastPoint.minute} minutes later"
        precipIntensity == 0.0 && lastPoint == null -> "ending in $minute minutes"
        precipIntensity == 0.0 && lastPoint != null -> "ending ${minute - lastPoint.minute} minutes later"
        else -> ""
    }
}

private fun String.toWeatherIcon(): ConditionCode {
    return when (this) {
        "freezing_rain_heavy" -> ConditionCode.SLEET
        "freezing_rain" -> ConditionCode.SLEET
        "freezing_rain_light" -> ConditionCode.SLEET
        "freezing_drizzle" -> ConditionCode.SLEET
        "ice_pellets_heavy" -> ConditionCode.HAIL
        "ice_pellets" -> ConditionCode.HAIL
        "ice_pellets_light" -> ConditionCode.HAIL
        "snow_heavy" -> ConditionCode.SNOW
        "snow" -> ConditionCode.SNOW
        "snow_light" -> ConditionCode.SNOW
        "flurries" -> ConditionCode.SNOW
        "tstorm" -> ConditionCode.THUNDERSTORM
        "rain_heavy" -> ConditionCode.RAIN
        "rain" -> ConditionCode.RAIN
        "rain_light" -> ConditionCode.RAIN
        "drizzle" -> ConditionCode.RAIN
        "fog_light" -> ConditionCode.FOG
        "fog" -> ConditionCode.FOG
        "cloudy" -> ConditionCode.CLOUDY
        "mostly_cloudy" -> ConditionCode.CLOUDY
        "partly_cloudy" -> ConditionCode.PARTLY_CLOUDY
        "mostly_clear" -> ConditionCode.CLEAR
        "clear" -> ConditionCode.CLEAR
        else -> ConditionCode.NONE
    }
}

private fun String.toMoonPhase(): MoonPhase {
    return when (this) {
        "new" -> MoonPhase.NEW_MOON
        "waxing_crescent" -> MoonPhase.WAXING_CRESCENT
        "first_quarter" -> MoonPhase.FIRST_QUARTER
        "waxing_gibbous" -> MoonPhase.WAXING_GIBBOUS
        "full" -> MoonPhase.FULL
        "waning_gibbous" -> MoonPhase.WANING_GIBBOUS
        "third_quarter" -> MoonPhase.THIRD_QUARTER
        "last_quarter" -> MoonPhase.THIRD_QUARTER
        "waning_crescent" -> MoonPhase.WANING_CRESCENT
        else -> throw Error("moon phase string was $this, not supported")

    }
}

private fun String?.toPrecipitationType(): PrecipitationType {
    return when (this) {
        "rain" -> PrecipitationType.Rain
        "snow" -> PrecipitationType.Snow
        "freezing_rain" -> PrecipitationType.Sleet
        "ice_pellets" -> PrecipitationType.Hail
        else -> PrecipitationType.None
    }
}