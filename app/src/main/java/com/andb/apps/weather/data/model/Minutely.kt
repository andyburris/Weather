package com.andb.apps.weather.data.model

data class Minutely(
    val summary: String,
    val data: List<MinutelyConditions>
) {

}