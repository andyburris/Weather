package com.andb.apps.weather.ui.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.CurrentLocationError
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.Machine
import com.andb.apps.weather.ui.common.ErrorItem
import com.andb.apps.weather.ui.theme.onBackgroundOverlay
import com.andb.apps.weather.ui.theme.onBackgroundSecondary

@Composable
fun LocationItem(
    location: LocationState,
    modifier: Modifier = Modifier,
    onAction: (Machine.Action) -> Unit
) {
    Row(
        modifier = modifier
            .then(when (location) {
                is LocationState.WithLocation -> Modifier.clickable { onAction.invoke(location.toSelectLocationAction()) }
                is LocationState.NoLocation -> Modifier
            })
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = when (location) {
            is LocationState.WithLocation -> Alignment.CenterVertically
            is LocationState.NoLocation -> Alignment.Top
        }
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colors.onBackgroundOverlay, CircleShape)
                .padding(8.dp),
        ) {
            Icon(
                imageVector = when (location) {
                    is LocationState.Current -> Icons.Outlined.MyLocation
                    is LocationState.Fixed -> Icons.Outlined.NearMe
                },
                contentDescription = null,
                tint = MaterialTheme.colors.onBackgroundSecondary,
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = when (location) {
                    is LocationState.Current -> "Current Location"
                    is LocationState.Fixed -> location.location.let { "${it.name}, ${it.region}" }
                },
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.subtitle1,
            )

            if (location is LocationState.Current.Error) {
                when (location.error) {
                    CurrentLocationError.NoPermission -> {
                        ErrorItem(
                            title = "Permission Needed",
                            actionIcon = Icons.Outlined.ChevronRight,
                            onClick = { onAction.invoke(Machine.Action.CurrentLocation.RequestPermission) }
                        )
                    }
                    CurrentLocationError.NoAccess -> ErrorItem(
                        title = "No Access",
                        actionIcon = Icons.Outlined.Refresh,
                        onClick = { onAction.invoke(Machine.Action.CurrentLocation.Refresh) }
                    )
                }
            }
        }
    }
}

private fun LocationState.toSelectLocationAction(): Machine.Action.SelectLocation = when (this) {
    is LocationState.Current -> Machine.Action.SelectLocation.Current
    is LocationState.Fixed -> Machine.Action.SelectLocation.Fixed(this.id)
}