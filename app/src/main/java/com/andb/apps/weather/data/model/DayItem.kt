package com.andb.apps.weather.data.model

data class DayItem(
    val day: DailyConditions,
    val hourly: List<HourlyConditions>
)