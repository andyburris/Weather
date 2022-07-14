package com.andb.apps.weather.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ConditionState
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.ui.theme.divider
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import kotlin.math.roundToInt

@Composable
fun UpperContent(
    locationState: LocationState.WithLocation,
    conditionState: ConditionState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
    ) {
        LocationHeader(
            locationState = locationState,
            modifier = Modifier.fillMaxWidth(),
            onClick = {} //TODO: open location dialog
        )
        Spacer(modifier = Modifier.weight(1f))
        when (conditionState) {
            is ConditionState.Error -> {}
            else -> {
                Column {
                    Text(
                        text = when (conditionState) {
                            is ConditionState.Ok -> "${conditionState.resource.current.temperature.roundToInt()}°"
                            else -> "68°"
                        },
                        style = MaterialTheme.typography.h1,
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.placeholder(
                            visible = conditionState is ConditionState.NotLoaded,
                            shape = RoundedCornerShape(8.dp),
                            highlight = PlaceholderHighlight.shimmer(Color.White.copy(alpha = 0.12f)),
                            color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.divider)
                        ),
                    )
                    Text(
                        text = when (conditionState) {
                            is ConditionState.Ok -> "Feels like ${conditionState.resource.current.apparentTemperature.roundToInt()}°"
                            else -> "Feels like 68°"
                        },
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.placeholder(
                            visible = conditionState is ConditionState.NotLoaded,
                            shape = CircleShape,
                            highlight = PlaceholderHighlight.shimmer(Color.White.copy(alpha = 0.12f)),
                            color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.divider)
                        ),
                    )
                }
            }
        }
    }
}
