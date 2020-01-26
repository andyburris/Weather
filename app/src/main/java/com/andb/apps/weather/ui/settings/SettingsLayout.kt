package com.andb.apps.weather.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.andb.apps.weather.BuildConfig
import com.andb.apps.weather.R
import com.andb.apps.weather.data.local.*
import com.andb.apps.weather.util.getColorCompat
import de.Maxr1998.modernpreferences.helpers.*
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

object SettingsLayout {
    fun create(context: Context) = screen(context) {
        preferenceFileName = KEY_SHAREDPREFS_NAME

        titleRes = R.string.settings_title
        categoryHeader("api_header") {
            titleRes = R.string.settings_api_header
        }
        dialog(KEY_API_KEY) {
            titleRes = R.string.settings_api_key_title
            summaryRes = R.string.settings_api_key_desc
            iconRes = R.drawable.ic_network_black_24dp
            contents {
                title(res = titleRes)
                input(
                    hintRes = summaryRes,
                    prefill = getString("")
                ) { materialDialog, charSequence ->
                    materialDialog.dismiss()
                    commitString(charSequence.toString())
                }
                cornerRadius(literalDp = 8f)
            }
        }

        categoryHeader("units_header") {
            titleRes = R.string.settings_units_header
        }

        //TODO: values same as display?
        chips<String>(KEY_UNIT_TEMP, "unit_temp_imperial") {
            titleRes = R.string.settings_units_temperature
            iconRes = R.drawable.ic_thermostat_black_24dp
            val items = context.resources.getStringArray(R.array.settings_units_temp_options)
            addChip("unit_temp_imperial", items[0], context.getColorCompat(R.color.colorAccent))
            addChip("unit_temp_metric", items[1], context.getColorCompat(R.color.colorAccent))
        }

        chips<String>(KEY_UNIT_DISTANCE, "unit_distance_imperial") {
            titleRes = R.string.settings_units_distance
            iconRes = R.drawable.ic_tape_measure
            val items = context.resources.getStringArray(R.array.settings_units_distance_options)
            addChip("in", items[0], context.getColorCompat(R.color.colorAccent))
            addChip("cm", items[1], context.getColorCompat(R.color.colorAccent))
        }

        chips<Boolean>(KEY_UNIT_TIME, false) {
            titleRes = R.string.settings_units_time
            iconRes = R.drawable.ic_access_time_black_24dp
            val items = context.resources.getStringArray(R.array.settings_units_time_options)
            addChip(false, items[0], context.getColorCompat(R.color.colorAccent))
            addChip(true, items[1], context.getColorCompat(R.color.colorAccent))
        }

        categoryHeader("graph_header") {
            titleRes = R.string.settings_graph_header
        }

        dialog(KEY_DAY_START) {
            titleRes = R.string.settings_graph_start_time
            iconRes = R.drawable.ic_weather_sunset
            summary = DateTimeFormatter.ofPattern("ha").format(LocalTime.of(Prefs.dayStart, 0))
            contents {
                val picker = HourPicker(context)
                customView(view = picker, noVerticalPadding = true)
                picker.is24Hour = Prefs.time24HrFormat
                picker.selectedHour = Prefs.dayStart
                this.positiveButton {
                    commitInt(picker.selectedHour)
                    summary =
                        DateTimeFormatter.ofPattern("ha").format(LocalTime.of(Prefs.dayStart, 0))
                    requestRebind()
                    it.dismiss()
                }
            }
        }

        dialog(KEY_DAY_END) {
            titleRes = R.string.settings_graph_end_time
            iconRes = R.drawable.ic_weather_sunset
            summary = DateTimeFormatter.ofPattern("ha").format(LocalTime.of(Prefs.dayEnd, 0))
            contents {
                val picker = HourPicker(context)
                customView(view = picker, noVerticalPadding = true)
                picker.is24Hour = Prefs.time24HrFormat
                picker.selectedHour = Prefs.dayEnd
                this.positiveButton {
                    commitInt(picker.selectedHour)
                    summary =
                        DateTimeFormatter.ofPattern("ha").format(LocalTime.of(Prefs.dayEnd, 0))
                    requestRebind()
                    it.dismiss()
                }
            }
        }


        categoryHeader("theme_header") {
            titleRes = R.string.settings_theme_header
        }

        dialog(KEY_NIGHT_MODE) {
            titleRes = R.string.settings_theme_night_mode_title
            iconRes = R.drawable.ic_weather_night
            contents {
                title(res = titleRes)
                val items =
                    context.resources.getStringArray(R.array.settings_theme_night_mode_options)
                        .toMutableList()
                items.removeAt(if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) 3 else 2)
                val initialSelection = when (getInt(Prefs.nightMode)) {
                    AppCompatDelegate.MODE_NIGHT_NO -> 0
                    AppCompatDelegate.MODE_NIGHT_YES -> 1
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> 2
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> 2
                    AppCompatDelegate.MODE_NIGHT_AUTO_TIME -> 3
                    else -> 0 //never should be called
                }

                listItemsSingleChoice(
                    items = items,
                    initialSelection = initialSelection,
                    waitForPositiveButton = true
                ) { dialog, index, text ->
                    when (index) {
                        0 -> commitInt(AppCompatDelegate.MODE_NIGHT_NO)
                        1 -> commitInt(AppCompatDelegate.MODE_NIGHT_YES)
                        2 -> commitInt(if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        3 -> commitInt(AppCompatDelegate.MODE_NIGHT_AUTO_TIME)
                    }
                    Prefs.nightMode = getInt(Prefs.nightMode)
                    AppCompatDelegate.setDefaultNightMode(Prefs.nightMode)
                }

                cornerRadius(literalDp = 8f)

            }
        }

        colorPicker(KEY_COLOR_TEMP) {
            titleRes = R.string.temperature
            summaryRes = R.string.settings_theme_temp_desc
            iconRes = R.drawable.ic_thermostat_black_24dp
            onSelect = {
                Prefs.colorTemperature = getInt(
                    ContextCompat.getColor(
                        context,
                        R.color.colorTemperatureBackgroundDefault
                    )
                )
            }
        }
        colorPicker(KEY_COLOR_RAIN) {
            titleRes = R.string.rain
            summaryRes = R.string.settings_theme_rain_desc
            iconRes = R.drawable.ic_raindrop_black_24dp
            onSelect = {
                Prefs.colorRain =
                    getInt(context.getColorCompat(R.color.colorRainBackgroundDefault))
            }
        }
        colorPicker(KEY_COLOR_UV) {
            titleRes = R.string.uv_index
            summaryRes = R.string.settings_theme_uv_desc
            iconRes = R.drawable.ic_uv_index_black_24dp
            onSelect = {
                Prefs.colorUVIndex =
                    getInt(context.getColorCompat(R.color.colorUVIndexBackgroundDefault))
            }
        }
        colorPicker(KEY_COLOR_WIND) {
            titleRes = R.string.wind
            summaryRes = R.string.settings_theme_wind_desc
            iconRes = R.drawable.ic_wind_black_24dp
            onSelect = {
                Prefs.colorWind =
                    getInt(context.getColorCompat(R.color.colorWindBackgroundDefault))
            }
        }

        categoryHeader("misc_header") {
            titleRes = R.string.settings_misc_header
        }

        subScreen {
            titleRes = R.string.settings_misc_about
            iconRes = R.drawable.ic_info_black_24dp

            categoryHeader("about_info_header") {
                titleRes = R.string.settings_misc_about_info
            }

            pref("about_info_version") {
                iconRes = R.drawable.ic_info_black_24dp
                titleRes = R.string.settings_misc_about_version
                summary = BuildConfig.VERSION_NAME
            }

            pref("about_info_source") {
                iconRes = R.drawable.ic_network_black_24dp
                titleRes = R.string.settings_misc_about_source
                onClicked {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.github.com/andb3/Weather")
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    return@onClicked true
                }
            }

            categoryHeader("about_licenses_header") {
                titleRes = R.string.settings_misc_about_licenses
            }
        }

        dialog("misc_help") {
            titleRes = R.string.settings_misc_help
            iconRes = R.drawable.ic_help_black_24dp
            contents {
                title(R.string.settings_misc_help)
                message(R.string.settings_misc_help_text)
                cornerRadius(8f)
            }
        }
    }
}