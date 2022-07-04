package com.andb.apps.weather.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ui.theme.divider
import com.andb.apps.weather.ui.theme.overlay

data class ChipPalette(val backgroundColor: Color, val textColor: Color, val borderColor: Color)

@Composable
fun Chip(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    deselectedPalette: ChipPalette = ChipPalette(
        backgroundColor = Color.Transparent,
        textColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
        borderColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.divider),
    ),
    selectedPalette: ChipPalette = ChipPalette(
        backgroundColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.overlay),
        textColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
        borderColor = Color.Transparent,
    ),
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val currentPalette = if (selected) selectedPalette else deselectedPalette
    Row(
        modifier = modifier
            .background(currentPalette.backgroundColor, CircleShape)
            .border(1.dp, currentPalette.borderColor, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .height(32.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Image(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(currentPalette.textColor)
            )
        }
        Text(
            text = label,
            color = currentPalette.textColor,
            style = MaterialTheme.typography.body2
        )
    }
}