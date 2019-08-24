package com.andb.apps.weather.objects

data class Minutely(
    val summary: String,
    val icon: WeatherIcon, //TODO: json converter from string
    val data: List<MinutelyConditions>
) {

}