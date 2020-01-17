package com.andb.apps.weather.data.local

import androidx.appcompat.app.AppCompatDelegate
import com.andb.apps.weather.BuildConfig
import com.andb.apps.weather.R
import com.andb.apps.weather.util.getColorCompat
import com.chibatching.kotpref.KotprefModel

const val KEY_SHAREDPREFS_NAME = "${BuildConfig.APPLICATION_ID}_preferences"

const val KEY_SELECTED_ID = "selected_id"
const val KEY_API_KEY = "api_key"
const val KEY_UNIT_TEMP = "units_temp"
const val KEY_UNIT_DISTANCE = "units_distance"
const val KEY_UNIT_TIME = "units_time"
const val KEY_DAY_START = "graph_day_start"
const val KEY_DAY_END = "graph_day_end"
const val KEY_NIGHT_MODE = "night_mode"
const val KEY_COLOR_TEMP = "color_temp"
const val KEY_COLOR_RAIN = "color_rain"
const val KEY_COLOR_UV = "color_uv"
const val KEY_COLOR_WIND = "color_wind"

object Prefs : KotprefModel() {
    override val kotprefName: String
        get() = KEY_SHAREDPREFS_NAME

    const val DEBUG_OFFLINE: Boolean = false
    var barWidth = 40

    var apiKey by stringPref("", KEY_API_KEY)

    var selectedID by stringPref("", KEY_SELECTED_ID)

    var nightMode by intPref(AppCompatDelegate.MODE_NIGHT_NO, KEY_NIGHT_MODE)

    var dayStart by intPref(7, KEY_DAY_START)
    var dayEnd by intPref(23, KEY_DAY_END)

    var colorTemperature by intPref(
        context.getColorCompat(R.color.colorTemperatureBackgroundDefault),
        KEY_COLOR_TEMP
    )
    var colorRain by intPref(
        context.getColorCompat(R.color.colorRainBackgroundDefault),
        KEY_COLOR_RAIN
    )
    var colorUVIndex by intPref(
        context.getColorCompat(R.color.colorUVIndexBackgroundDefault),
        KEY_COLOR_UV
    )
    var colorWind by intPref(
        context.getColorCompat(R.color.colorWindBackgroundDefault),
        KEY_COLOR_WIND
    )

    var rainUnit by stringPref("in", KEY_UNIT_DISTANCE)
    var tempUnit by stringPref("F", KEY_UNIT_TEMP)
    var time24HrFormat by booleanPref(false, KEY_UNIT_TIME)

}
