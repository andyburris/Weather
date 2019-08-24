package com.andb.apps.weather.objects

import com.squareup.moshi.Json

enum class WeatherIcon {
    @field:Json(name = "clear-day")
    CLEAR_DAY,
    @field:Json(name = "clear-night")
    CLEAR_NIGHT,
    @field:Json(name = "rain")
    RAIN,
    @field:Json(name = "snow")
    SNOW,
    @field:Json(name = "sleet")
    SLEET,
    @field:Json(name = "wind")
    WIND,
    @field:Json(name = "fog")
    FOG,
    @field:Json(name = "cloudy")
    CLOUDY,
    @field:Json(name = "partly-cloudy-day")
    PARTLY_CLOUDY_DAY,
    @field:Json(name = "partly-cloudy-night")
    PARTLY_CLOUDY_NIGHT,
    @field:Json(name = "none")
    NONE
}