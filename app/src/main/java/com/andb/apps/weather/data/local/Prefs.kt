package com.andb.apps.weather.data.local

import androidx.appcompat.app.AppCompatDelegate
import com.andb.apps.weather.BuildConfig
import com.andb.apps.weather.data.model.UnitType
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.enumpref.enumValuePref
import com.squareup.moshi.Moshi
import org.koin.core.KoinComponent
import org.koin.core.inject

const val KEY_SHAREDPREFS_NAME = "${BuildConfig.APPLICATION_ID}_preferences"

const val PROVIDER_DARK_SKY = "darksky"
const val PROVIDER_CLIMACELL = "climacell"
const val PROVIDER_ACCUWEATHER = "accuweather"
const val PROVIDER_NWS = "nws"

const val KEY_PROVIDER = "provider"
const val KEY_SELECTED_ID = "selected_id"
const val KEY_API_KEY = "api_key"
const val KEY_UNITS = "units_temp"
const val KEY_UNIT_TIME = "units_time"
const val KEY_DAY_START = "graph_day_start"
const val KEY_DAY_END = "graph_day_end"
const val KEY_NIGHT_MODE = "night_mode"
const val KEY_COLOR_TEMP = "color_temp"
const val KEY_COLOR_RAIN = "color_rain"
const val KEY_COLOR_UV = "color_uv"
const val KEY_COLOR_WIND = "color_wind"

object Prefs : KotprefModel(), KoinComponent {

    private val moshi: Moshi by inject()

    override val kotprefName: String
        get() = KEY_SHAREDPREFS_NAME

    var apiKey by stringPref("", KEY_API_KEY)

    var selectedID by stringPref("", KEY_SELECTED_ID)

    var nightMode by intPref(AppCompatDelegate.MODE_NIGHT_NO, KEY_NIGHT_MODE)

    var dayStart by intPref(7, KEY_DAY_START)
    var dayEnd by intPref(23, KEY_DAY_END)

    var units by enumValuePref(UnitType.US, KEY_UNITS)
    var time24HrFormat by booleanPref(false, KEY_UNIT_TIME)

}

