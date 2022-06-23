package com.andb.apps.weather.ui.test.background

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.andb.apps.weather.data.model.ConditionCode
import com.andb.apps.weather.ui.theme.WeatherColors

@Composable
fun WeatherBackground(
    conditionCode: ConditionCode,
    modifier: Modifier = Modifier,
    onRotationChange: (rotation2D: Float, rotation3D: Float) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val rotation = context.getRotationState().value
    LaunchedEffect(rotation) {
        onRotationChange.invoke(rotation.rotation2D, rotation.rotation3D)
    }
    Background(conditionCode = conditionCode, modifier = modifier) {
        when (conditionCode) {
            ConditionCode.CLEAR -> Sun(rotationState = rotation)
            ConditionCode.CLOUDY -> Clouds(rotationState = rotation)
            ConditionCode.FOG -> Fog(modifier.fillMaxSize())
            ConditionCode.HAIL -> Precipitation(config = hailConfig, rotationState = rotation)
            ConditionCode.NONE -> Box {}
            ConditionCode.PARTLY_CLOUDY -> Clouds(rotationState = rotation)
            ConditionCode.RAIN -> Precipitation(config = rainConfig, rotationState = rotation)
            ConditionCode.SLEET -> {
                Precipitation(config = SleetConfig.rain, rotationState = rotation)
                Precipitation(config = SleetConfig.snow, rotationState = rotation)
            }
            ConditionCode.SNOW -> Precipitation(config = snowConfig, rotationState = rotation)
            ConditionCode.THUNDERSTORM -> {
                Clouds(rotationState = rotation, modifier = Modifier.fillMaxSize())
                Precipitation(config = rainConfig, rotationState = rotation)
            }
            ConditionCode.WIND -> Box {}
        }
    }
}

@Composable
private fun Background(
    conditionCode: ConditionCode,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit
) = Box(
    modifier = modifier.background(WeatherColors.skyColor(conditionCode).first),
    content = content
)

private val rainConfig = PrecipiationConfig(
    colors = WeatherColors.Overlay.Rain.values().map { it.color },
    lengths = (12 until 48).step(4).toList(),
    speeds = listOf(500, 750, 1000),
    baseAngleOffsetDeg = -10f,
    dampenRotation = 0.75f,
)

private val snowConfig = PrecipiationConfig(
    colors = WeatherColors.Overlay.Snow.values().map { it.color },
    lengths = (8..8).toList(),
    speeds = listOf(3450, 3500, 3550),
    dampenRotation = 0.25f,
)

private val hailConfig = PrecipiationConfig(
    colors = WeatherColors.Overlay.Hail.values().map { it.color },
    lengths = (8..16).step(2).toList(),
    speeds = listOf(500, 750),
    dampenRotation = 0.2f,
    amountOfDroplets = 50,
)

private object SleetConfig {
    val rain = rainConfig.copy(
        colors = listOf(
            WeatherColors.Overlay.Sleet.primaryRainDroplet.color,
            WeatherColors.Overlay.Sleet.secondaryRainDroplet.color
        ),
        speeds = rainConfig.speeds.map { it + 500 },
        amountOfDroplets = 50
    )
    val snow = snowConfig.copy(speeds = snowConfig.speeds.map { it - 500 })
}