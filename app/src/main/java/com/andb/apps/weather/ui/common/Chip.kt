package com.andb.apps.weather.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Chip(label: String, modifier: Modifier = Modifier, selected: Boolean = false) {
    Box(
        modifier = modifier then if (selected) {
            Modifier.background(
                color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
                shape = CircleShape
            )
        } else {
            Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium),
                shape = CircleShape
            )
        }
            .height(32.dp)
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.align(Alignment.Center),
            color = if (selected) MaterialTheme.colors.background else MaterialTheme.colors.onBackground.copy(
                alpha = ContentAlpha.medium
            )
        )
    }
}