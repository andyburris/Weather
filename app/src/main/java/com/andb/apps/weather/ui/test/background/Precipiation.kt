package com.andb.apps.weather.ui.test.background

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal data class PrecipiationConfig(
    val colors: List<Color>,
    val speeds: List<Int>,
    val lengths: List<Int>,
    val amountOfDroplets: Int = 100,
    val baseAngleOffsetDeg: Float = 0f,
    val dampenRotation: Float = 1f,
)

private data class Droplet(val color: Color, val length: Dp, var startingX: Dp)

@Composable
internal fun Precipitation(
    config: PrecipiationConfig,
    rotationState: RotationState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val hypotenuse = sqrt(this.minHeight.value.pow(2) + this.minWidth.value.pow(2))
        val startingXPositions = -(hypotenuse / 2).toInt() until (hypotenuse / 2).toInt()
        val droplets = remember {
            (0 until config.amountOfDroplets).map {
                Droplet(
                    config.colors.random(),
                    config.lengths.random().dp,
                    startingXPositions.random().dp
                )
            }
        }

        val transition = rememberInfiniteTransition()
        val animations = config.speeds.map { speed ->
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(speed, easing = LinearEasing))
            )
        }
        droplets.forEach { droplet ->
            val startingPercent = remember { (0..100).random() / 100f }
            val animation = remember { animations.random() }
            val fallPercent = (animation.value + startingPercent) % 1f
            val fallMagnitude = hypotenuse * fallPercent
            val offset2DRotation =
                dampenRotation(
                    rotationState.rotation2D,
                    config.dampenRotation
                ) - config.baseAngleOffsetDeg.toRadians()
            Box(
                modifier = Modifier
                    .offset(
                        x = droplet.startingX + (fallMagnitude * cos(offset2DRotation)).dp,
                        y = (fallMagnitude * sin(offset2DRotation)).dp
                    )
                    .rotate(
                        offset2DRotation
                            .toDegrees()
                            .toFloat()
                    )
                    .height(8.dp)
                    .width(droplet.length)
                    .background(droplet.color, CircleShape)
            )
        }
    }
}

private fun dampenRotation(
    rotation: Float,
    dampedPercent: Float,
    center: Float = 90f.toRadians().toFloat()
): Float {
    val baseDeviation = rotation - center
    val dampenedDeviation = dampedPercent * baseDeviation
    return center + dampenedDeviation
}