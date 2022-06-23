package com.andb.apps.weather.ui.test.background

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.andb.apps.weather.ui.common.InfiniteMarqueeBox
import com.andb.apps.weather.ui.theme.WeatherColors

@Composable
internal fun Clouds(rotationState: RotationState, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val minCloudSize = this.minWidth * 2 / 3
        val topClouds = remember {
            (0 until 3).map { (minCloudSize.value.toInt() until minCloudSize.value.toInt() * 3 / 2).random().dp }
        }
        Column(
            modifier = Modifier
                .offset(y = (-64).dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InfiniteMarqueeBox(
                durationMillis = 25000,
            ) { animationPercent ->
                Row(
                    modifier = Modifier.padding(end = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    topClouds.forEach { cloudWidth ->
                        Cloud(modifier = Modifier.requiredSize(width = cloudWidth, height = 160.dp))
                    }
                }
            }

            InfiniteMarqueeBox(
                durationMillis = 5000,
                modifier = Modifier.fillMaxWidth()
            ) {
                Cloud(
                    modifier = Modifier.requiredSize(width = 208.dp, height = 96.dp)
                )
            }
        }
    }
}

@Composable
private fun Cloud(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(WeatherColors.Overlay.clearCloud, CircleShape)) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .background(WeatherColors.Overlay.clearCloud, CircleShape)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .background(WeatherColors.Overlay.clearCloud, CircleShape)
                    .fillMaxSize()
            )
        }
    }
}

@Composable
internal fun CloudsLegacy(rotationState: RotationState, modifier: Modifier = Modifier) {
    val clouds = (0..1).flatMap { level -> (-1..1).map { level to it } }
    fun BoxWithConstraintsScope.cloudSize(level: Int): Dp {
        val baseSize = this.maxWidth / 2
        val levelMultiplier = 1 + (0.5 * level)
        return baseSize * levelMultiplier.toFloat()
    }
    BoxWithConstraints(modifier = modifier) {
        clouds.forEach { (level, horizontal) ->
            val rotation3D: Float =
                animateFloatAsState(targetValue = rotationState.rotation3D).value
            val breathe = rememberInfiniteTransition().animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            ).value
            val cloudSize = cloudSize(level) + (rotation3D * 40).dp + (breathe * 40).dp
            val cloudOffset = cloudSize * 2 / 3
            val cloudAlpha = 0.15 - (.1 * level)
            Box(
                modifier = Modifier
                    .offset(x = horizontal * cloudOffset, y = 0.dp - (cloudSize / 2))
                    .background(
                        color = WeatherColors.Overlay.clearCloud.copy(alpha = cloudAlpha.toFloat()),
                        shape = CircleShape
                    )
                    .size(cloudSize)
                    .align(Alignment.TopCenter)
            )
        }
    }
}