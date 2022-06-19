package com.andb.apps.weather.data.local

import androidx.appcompat.app.AppCompatDelegate
import com.andb.apps.weather.BuildConfig
import com.andb.apps.weather.R
import com.andb.apps.weather.data.model.Provider
import com.andb.apps.weather.data.model.UnitType
import com.andb.apps.weather.util.getColorCompat
import com.andb.apps.weather.util.listPref
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

    const val DEBUG_OFFLINE: Boolean = true
    const val MULTIPLE_PROVIDERS: Boolean = false
    var barWidth = 40

    var providers by listPref<Provider>(
        key = KEY_PROVIDER,
        default = listOf(
            Provider(PROVIDER_CLIMACELL, "Climacell", true, 4),
            Provider(PROVIDER_DARK_SKY, "Dark Sky", true, 7),
            Provider(PROVIDER_ACCUWEATHER, "AccuWeather", false, 5),
            Provider(PROVIDER_NWS, "National Weather Service", false, 5)
        ),
        adapter = moshi.adapter(Provider::class.java),
        commitByDefault = true
    )
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

    var units by enumValuePref(UnitType.US, KEY_UNITS)
    var time24HrFormat by booleanPref(false, KEY_UNIT_TIME)

}

