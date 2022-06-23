package com.andb.apps.weather.ui.theme

import androidx.compose.ui.graphics.Color
import com.andb.apps.weather.data.model.ConditionCode

object WeatherColors {
    fun skyColor(conditionCode: ConditionCode) = when (conditionCode) {
        ConditionCode.CLEAR -> Color(0xFFEDBA1E) to Color.Black
        ConditionCode.RAIN -> Color(0xFF0679FF) to Color.Black
        ConditionCode.THUNDERSTORM -> Color(0xFF7491F9) to Color(0xFF242F57)
        ConditionCode.SNOW -> Color(0xFFD5E8FF) to Color.Black
        ConditionCode.SLEET -> Color(0xFFC4D2DE) to Color.Black
        ConditionCode.HAIL -> Color(0xFFD5E8FF) to Color.Black
        ConditionCode.WIND -> Color.White to Color.Black
        ConditionCode.FOG -> Color(0xFFC5CDD5) to Color.Black
        ConditionCode.CLOUDY -> Color(0xFF88AACD) to Color.Black
        ConditionCode.PARTLY_CLOUDY -> Color(0xFF3CADFF) to Color.Black
        ConditionCode.NONE -> Color.White to Color.Black
    }

    object Overlay {
        val clearSunOverlay: Color = Color.Black.copy(alpha = 0.05f)
        val fogOverlay: Color = Color.Black.copy(alpha = 0.05f)

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