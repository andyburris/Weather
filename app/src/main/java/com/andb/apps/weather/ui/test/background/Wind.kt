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
import com.andb.apps.weather.ui.theme.WeatherColors

private const val amountOfWindLines = 8

@Composable
internal fun Wind(rotationState: RotationState, daytime: Boolean, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val lines = remember {
            (0 until amountOfWindLines).map { randomWindLine(this.minWidth) }
        }
        Column(
            modifier = Modifier
                .padding(vertical = 64.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            lines.forEachIndexed { i, line ->
                val animationSpeed = remember { (750 until 1000).random() }
                InfiniteMarqueeBox(
                    durationMillis = animationSpeed,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        line.forEach { segment ->
                            Box(
                                modifier = Modifier
                                    .background(WeatherColors.Overlay.fog(daytime), CircleShape)
                                    .requiredSize(width = segment, height = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun randomWindLine(totalLength: Dp): List<Dp> {
    val minSegmentLength = 16.dp
    val maxSegmentLength = totalLength / 2
    val segments = mutableListOf<Dp>()
    while (segments.sumOf { it.value.toInt() + 8 } < totalLength.value) {
        segments += (minSegmentLength.value.toInt() until maxSegmentLength.value.toInt()).random().dp
    }
    return segments
}