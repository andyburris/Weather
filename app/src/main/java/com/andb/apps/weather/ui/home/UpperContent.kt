package com.andb.apps.weather.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ConditionState
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.ui.theme.divider
import com.andb.apps.weather.ui.theme.onPrimarySecondary
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer
import kotlin.math.roundToInt

@Composable
fun UpperContent(
    locationState: LocationState.WithLocation,
    conditionState: ConditionState,
    scrollAmount: Dp,
    modifier: Modifier = Modifier,
    onOpenLocationPicker: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        LocationHeader(
            locationState = locationState,
            modifier = Modifier
                .padding(top = scrollAmount)
                .fillMaxWidth(),
            onClick = onOpenLocationPicker
        )
        Spacer(modifier = Modifier.weight(1f))
        when (conditionState) {
            is ConditionState.Error -> {}
            else -> {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = when (conditionState) {
                            is ConditionState.Ok -> "${conditionState.resource.current.temperature.roundToInt()}°"
                            else -> "68°"
                        },
                        style = MaterialTheme.typography.h1,
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier
                            .placeholder(
                                visible = conditionState is ConditionState.NotLoaded,
                                shape = RoundedCornerShape(8.dp),
                                highlight = PlaceholderHighlight.shimmer(Color.White.copy(alpha = 0.12f)),
                                color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.divider)
                            )
                            .alignBy(LastBaseline),
                    )
                    Text(
                        text = when (conditionState) {
                            is ConditionState.Ok -> "Feels like ${conditionState.resource.current.apparentTemperature.roundToInt()}°"
                            else -> "Feels like 68°"
                        },
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onPrimarySecondary,
                        modifier = Modifier
                            .placeholder(
                                visible = conditionState is ConditionState.NotLoaded,
                                shape = CircleShape,
                                highlight = PlaceholderHighlight.shimmer(Color.White.copy(alpha = 0.12f)),
                                color = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.divider)
                            )
                            .alignBy(LastBaseline),
                    )
                }
            }
        }
    }
}
