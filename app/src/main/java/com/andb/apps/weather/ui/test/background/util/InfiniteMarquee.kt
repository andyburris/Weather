package com.andb.apps.weather.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout

enum class MarqueeDirection {
    LeftToRight, RightToLeft
}

@Composable
fun InfiniteMarqueeBox(
    durationMillis: Int,
    modifier: Modifier = Modifier,
    direction: MarqueeDirection = MarqueeDirection.LeftToRight,
    content: @Composable BoxScope.(animationPercent: Float) -> Unit,
) {
    val animationPercent = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing))
    ).value
    Layout(
        modifier = modifier
            //prevents contents larger than screen size from getting clipped
            .horizontalScroll(rememberScrollState(), enabled = false)
            .clipToBounds(),
        content = {
            // Wrap in boxes so we only have to deal with 2 layout nodes.
            Box { content(animationPercent) }
            Box { content(animationPercent) }
        }) { measurables, constraints ->
        val (main, overflow) = measurables.map { it.measure(constraints) }
        layout(main.width, main.height) {
            val directionMultiplier = if (direction == MarqueeDirection.LeftToRight) 1 else -1
            val offset = (main.width * animationPercent * directionMultiplier).toInt()
            main.placeRelative(offset, 0)
            overflow.placeRelative(-main.width * directionMultiplier + offset, 0)
        }
    }
}