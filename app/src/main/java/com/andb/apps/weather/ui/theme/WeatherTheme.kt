package com.andb.apps.weather.ui.theme

import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.andb.apps.weather.data.model.ConditionCode
import com.andb.apps.weather.ui.home.HomeView

object WeatherColors {
    data class ViewColors(val background: Color, val text: Color, val graphic: Color)

    fun ViewColors(background: Long, text: Long, graphic: Long) =
        ViewColors(Color(background), Color(text), Color(graphic))

    @Composable
    fun viewColors(view: HomeView) = when (view) {
        HomeView.Summary -> ViewColors(
            background = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
            text = MaterialTheme.colors.background,
            graphic = Color.Black
        )
        HomeView.Temperature -> ViewColors(
            background = 0xFFF6D5E2,
            text = 0xFF7B201E,
            graphic = 0xFFF08383
        )
        HomeView.Rain -> ViewColors(
            background = 0xFFBEE0EB,
            text = 0xFF013A63,
            graphic = 0xFF83D6F0
        )
        HomeView.UV -> ViewColors(background = 0xFFFCF1D2, text = 0xFF614900, graphic = 0xFFEDBA1E)
        HomeView.Wind -> ViewColors(
            background = 0xFFD8F3DC,
            text = 0xFF1B4332,
            graphic = 0xFF27AE60
        )
    }

    fun ConditionCode.skyColor(daytime: Boolean) = when (this) {
        ConditionCode.CLEAR -> Color(0xFFEDBA1E) to Color(0xFF101112)
        ConditionCode.RAIN -> Color(0xFF0679FF) to Color(0xFF023E7D)
        ConditionCode.THUNDERSTORM -> Color(0xFF7491F9) to Color(0xFF242F57)
        ConditionCode.SNOW -> Color(0xFFD5E8FF) to Color(0xFF2B2E30)
        ConditionCode.SLEET -> Color(0xFFC4D2DE) to Color(0xFF222C34)
        ConditionCode.HAIL -> Color(0xFFD5E8FF) to Color(0xFF222C34)
        ConditionCode.WIND -> Color(0xFF27AE60) to Color(0xFF111C16)
        ConditionCode.FOG -> Color(0xFF7A8085) to Color(0xFF17191C)
        ConditionCode.CLOUDY -> Color(0xFF88AACD) to Color(0xFF131420)
        ConditionCode.PARTLY_CLOUDY -> Color(0xFF3CADFF) to Color(0xFF0F212E)
        ConditionCode.NONE -> Color.White to Color.Black
    }.let { if (daytime) it.first else it.second }

    object Overlay {
        fun clear(daytime: Boolean) = when (daytime) {
            true -> Color.White.copy(alpha = 0.20f)
            false -> Color.White.copy(alpha = 0.15f)
        }

        fun fog(daytime: Boolean) = when (daytime) {
            true -> Color.Black.copy(alpha = 0.05f)
            false -> Color.White.copy(alpha = 0.05f)
        }

        enum class Rain(val color: Color) {
            primaryDroplet(Color(0x1FFFFFFF)),
            secondaryDroplet(Color(0x0DFFFFFF)),
        }

        enum class Snow(val color: Color) {
            primaryDroplet(Color(0xF0FFFFFF)),
            secondaryDroplet(Color(0x80FFFFFF)),
        }

        enum class Hail(val color: Color) {
            primaryDroplet(Color(0xF0FFFFFF)),
            secondaryDroplet(Color(0xBFFFFFFF)),
        }

        enum class Sleet(val color: Color) {
            primaryRainDroplet(Color(0x80FFFFFF)),
            secondaryRainDroplet(Color(0x1FFFFFFF)),
            primarySnowDroplet(Color(0xF0FFFFFF)),
            secondarySnowDroplet(Color(0x80FFFFFF)),
        }

        val clearCloud: Color = Color(0x26FFFFFF)
    }
}