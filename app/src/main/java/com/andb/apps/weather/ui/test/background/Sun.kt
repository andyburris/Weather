package com.andb.apps.weather.ui.test.background

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ui.theme.WeatherColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val OctagonShape = GenericShape { size, layoutDirection ->
    val startOffset = Offset(size.width / 2, 0f)
    moveTo(startOffset.x, startOffset.y)
    val sideLength = size.height / sqrt(4 + 2 * sqrt(2f))
    (1 until 8).fold(startOffset) { acc, sideIndex ->
        val angle = sideIndex * (-45).toRadians() - (-45 / 2).toRadians()
        val next =
            acc.minus(Offset(sideLength * cos(angle).toFloat(), sideLength * sin(angle).toFloat()))
        lineTo(next.x, next.y)
        next
    }
    lineTo(startOffset.x, startOffset.y)
}

@Composable
internal fun Sun(rotationState: RotationState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        (0 until 6).forEach { level ->
            val size = 208.dp + (96.dp * level)
            val rotation = rememberInfiniteTransition().animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        10000 - (level * 100),
                        easing = LinearEasing
                    )
                )
            )
            Box(
                modifier = Modifier
                    .offset(y = -size / 2)
                    .align(Alignment.TopCenter)
                    .rotate(rotation.value)
                    .drawWithContent {
                        val paint = Paint().apply {
                            color = WeatherColors.Overlay.clearSunOverlay
                            pathEffect =
                                PathEffect.cornerPathEffect(with(Density(this@drawWithContent.density)) { 16.dp.toPx() })
                        }
                        this.drawIntoCanvas {
                            it.drawOutline(
                                outline = OctagonShape.createOutline(
                                    this.size,
                                    this.layoutDirection,
                                    Density(this.density)
                                ),
                                paint = paint
                            )
                        }

                    }
                    .background(WeatherColors.Overlay.clearSunOverlay, shape = OctagonShape)
                    .requiredSize(size)
            )
        }
    }
}