package com.andb.apps.weather.data.model

data class Hourly(
    val summary: String,
    val icon: WeatherIcon,
    val data: List<HourlyConditions>
)