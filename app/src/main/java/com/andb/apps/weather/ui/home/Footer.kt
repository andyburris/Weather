package com.andb.apps.weather.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ui.theme.onBackgroundSecondary

@Composable
fun Footer(modifier: Modifier = Modifier, onOpenSettings: () -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Attribution(Modifier.weight(1f))
        IconButton(
            modifier = Modifier.requiredSize(24.dp),
            onClick = onOpenSettings
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Open Settings",
                tint = MaterialTheme.colors.onBackgroundSecondary
            )
        }
    }
}

@Composable
private fun Attribution(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "Powered By",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onBackgroundSecondary
        )
        Text(
            text = "Weather",
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onBackground
        )
    }
}