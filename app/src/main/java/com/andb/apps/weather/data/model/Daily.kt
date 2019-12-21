package com.andb.apps.weather.data.model

data class Daily(
    val summary: String,
    val icon: WeatherIcon,
    val data: List<DailyConditions>
)