package com.andb.apps.weather.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.andb.apps.weather.R

object Prefs {
    const val DEBUG_OFFLINE: Boolean = false

    lateinit var prefs: SharedPreferences
    lateinit var apiKey: String

    var selectedID = ""

    var nightMode: Int = AppCompatDelegate.MODE_NIGHT_NO

    var dayStart: Int = -1
    var dayEnd: Int = -1

    var colorTemperature: Int = -1
    var colorRain: Int = -1
    var colorUVIndex: Int = -1
    var colorWind: Int = -1

    var rainUnit = "in"

    var barWidth = 40

    fun init(context: Context) {
        prefs =
            context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

        nightMode = prefs.getInt(
            KEY_NIGHT_MODE,
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        apiKey = prefs.getString(KEY_API_KEY, "") ?: ""

        dayStart = prefs.getInt(KEY_DAY_START, 7)
        dayEnd = prefs.getInt(KEY_DAY_END, 23)

        colorTemperature = prefs.getInt(
            KEY_COLOR_TEMP,
            ContextCompat.getColor(context, R.color.colorTemperatureBackgroundDefault)
        )
        colorRain = prefs.getInt(
            KEY_COLOR_RAIN,
            ContextCompat.getColor(context, R.color.colorRainBackgroundDefault)
        )
        colorUVIndex = prefs.getInt(
            KEY_COLOR_UV,
            ContextCompat.getColor(context, R.color.colorUVIndexBackgroundDefault)
        )
        colorWind = prefs.getInt(
            KEY_COLOR_WIND,
            ContextCompat.getColor(context, R.color.colorWindBackgroundDefault)
        )
    }

    const val KEY_API_KEY = "api_key"
    const val KEY_UNIT_TEMP = "units_temp"
    const val KEY_UNIT_DISTANCE = "units_distance"
    const val KEY_DAY_START = "graph_day_start"
    const val KEY_DAY_END = "graph_day_end"
    const val KEY_NIGHT_MODE = "night_mode"
    const val KEY_COLOR_TEMP = "color_temp"
    const val KEY_COLOR_RAIN = "color_rain"
    const val KEY_COLOR_UV = "color_uv"
    const val KEY_COLOR_WIND = "color_wind"

}