package com.andb.apps.weather.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ui.theme.onBackgroundDivider
import com.andb.apps.weather.ui.theme.onBackgroundSecondary
import com.andb.apps.weather.ui.theme.onBackgroundTertiary

@Composable
fun ErrorItem(
    title: String,
    actionIcon: ImageVector,
    modifier: Modifier = Modifier,
    description: String? = null,
    leadingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) = ErrorItem(
    title = title,
    actionIcon = {
        Icon(
            imageVector = actionIcon,
            contentDescription = null,
            tint = MaterialTheme.colors.onBackgroundSecondary,
        )
    },
    modifier = modifier,
    description = description,
    leadingIcon = leadingIcon?.let {
        {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = MaterialTheme.colors.onBackgroundTertiary,
            )
        }
    },
    onClick = onClick
)

@Composable
fun ErrorItem(
    title: String,
    actionIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .border(1.dp, MaterialTheme.colors.onBackgroundDivider, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.subtitle1,
            )
            if (description != null) {
                Text(
                    text = description,
                    color = MaterialTheme.colors.onBackgroundSecondary,
                    style = MaterialTheme.typography.body1,
                )
            }
        }
        actionIcon()
    }
}