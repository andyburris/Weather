package com.andb.apps.weather.ui.common

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.andb.apps.weather.ui.theme.onBackgroundDivider
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer

fun Modifier.placeholderOnLoading(
    color: Color? = null,
    shape: Shape = CircleShape,
    highlight: PlaceholderHighlight? = PlaceholderHighlight.shimmer(Color.White.copy(alpha = 0.12f)),
) = composed {
    val isLoading = LocalIsLoading.current
    this.placeholder(
        visible = isLoading,
        color = color ?: MaterialTheme.colors.onBackgroundDivider,
        shape = shape,
        highlight = highlight,
    )
}

val LocalIsLoading = compositionLocalOf { false }

@Composable
fun ProvideIsLoading(isLoading: Boolean = true, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalIsLoading provides isLoading) {
        content()
    }
}