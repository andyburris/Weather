package com.andb.apps.weather.ui.daily

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import com.andb.apps.weather.R
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.DailyConditions
import com.andb.apps.weather.data.model.MoonPhase
import com.andb.apps.weather.util.chipTextFrom
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.details_item.view.*
import org.threeten.bp.format.DateTimeFormatter

fun detailsAdapter(conditions: DailyConditions, context: Context) = Klaster.get()
    .itemCount(6)
    .view(R.layout.details_item, LayoutInflater.from(context))
    .bind { pos ->
        val detailsItem: DetailsItem = when (pos) {
            //TODO: feels like
            0 -> DetailsItem(
                R.string.rain,
                String.format(
                    context.resources.getString(R.string.percent_placeholder),
                    (conditions.precipProbability * 100).toInt()
                ),
                R.drawable.ic_raindrop_black_24dp,
                Prefs.colorRain
            )
            1 -> DetailsItem(
                R.string.uv_index,
                String.format(
                    context.resources.getString(R.string.max_placeholder),
                    conditions.uvIndex
                ),
                R.drawable.ic_uv_index_black_24dp,
                Prefs.colorUVIndex
            )
            2 -> DetailsItem(
                R.string.wind,
                String.format(
                    context.resources.getString(R.string.max_placeholder_mph),
                    conditions.windSpeed.toInt()
                ),
                R.drawable.ic_wind_black_24dp,
                Prefs.colorWind
            )
            3 -> {
                val formatter = DateTimeFormatter.ofPattern("h:mma")
                DetailsItem(
                    R.string.details_sunrise_sunset,
                    "${formatter.format(conditions.sunriseTime).toLowerCase()} | ${formatter.format(
                        conditions.sunsetTime
                    ).toLowerCase()}",
                    R.drawable.ic_weather_sunset,
                    Color.parseColor("#C4C4C4")
                )
            }
            4 -> DetailsItem(
                R.string.details_humidity,
                String.format(
                    context.resources.getString(R.string.percent_placeholder),
                    (conditions.humidity * 100).toInt()
                ),
                R.drawable.ic_water_percent,
                Color.parseColor("#C4C4C4")
            )
            else -> {
                val moonPhaseIndex = when (conditions.moonPhase) {
                    MoonPhase.NEW_MOON -> 0
                    MoonPhase.WAXING_CRESCENT -> 1
                    MoonPhase.FIRST_QUARTER -> 2
                    MoonPhase.WAXING_GIBBOUS -> 3
                    MoonPhase.FULL -> 4
                    MoonPhase.WANING_GIBBOUS -> 5
                    MoonPhase.THIRD_QUARTER -> 6
                    MoonPhase.WANING_CRESCENT -> 7
                }
                val moonPhase =
                    context.resources.getStringArray(R.array.details_moon_states)[moonPhaseIndex]
                DetailsItem(
                    R.string.details_moon,
                    moonPhase,
                    R.drawable.ic_weather_night,
                    Color.parseColor("#C4C4C4")
                )
            }
        }

        itemView.apply {
            detailsItemTitle.setText(detailsItem.titleRes)
            detailsItemDescription.text = detailsItem.details
            detailsItemIcon.setImageResource(detailsItem.iconRes)
            detailsItemCircle.color = detailsItem.color
            detailsItemIcon.setColorFilter(chipTextFrom(detailsItem.color))
        }
    }
    .build()

class DetailsItem(val titleRes: Int, val details: String, val iconRes: Int, val color: Int)
