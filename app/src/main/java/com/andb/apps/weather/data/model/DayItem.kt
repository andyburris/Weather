package com.andb.apps.weather.data.model

import org.threeten.bp.ZoneOffset

data class DayItem(
    val day: DailyConditions,
    val hourly: List<HourlyConditions>,
    val timeZone: ZoneOffset
)