package com.andb.apps.weather.ui.test.background

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ui.common.InfiniteMarqueeBox
import com.andb.apps.weather.ui.common.MarqueeDirection
import com.andb.apps.weather.ui.theme.WeatherColors

private const val amountOfFogLines = 16


@Composable
internal fun Fog(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val lines = remember {
            (0 until amountOfFogLines).map { randomFogLine(this.minWidth) }
        }
        Column(
            modifier = Modifier
                .padding(vertical = 64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            lines.forEachIndexed { i, line ->
                val animationSpeed = remember { (9000 until 10000).random() }
                val animationDirection =
                    remember { if (i % 2 == 0) MarqueeDirection.LeftToRight else MarqueeDirection.RightToLeft }
                InfiniteMarqueeBox(
                    durationMillis = animationSpeed,
                    modifier = Modifier.fillMaxWidth(),
                    direction = animationDirection,
                ) {
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        line.forEach { segment ->
                            Box(
                                modifier = Modifier
                                    .background(WeatherColors.Overlay.fogOverlay, CircleShape)
                                    .requiredSize(width = segment, height = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun randomFogLine(totalLength: Dp): List<Dp> {
    val minSegmentLength = 16.dp
    val maxSegmentLength = totalLength / 2
    val segments = mutableListOf<Dp>()
    while (segments.sumOf { it.value.toInt() + 8 } < totalLength.value) {
        segments += (minSegmentLength.value.toInt() until maxSegmentLength.value.toInt()).random().dp
    }
    return segments
}