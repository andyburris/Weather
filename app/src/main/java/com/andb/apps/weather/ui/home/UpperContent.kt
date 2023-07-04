package com.andb.apps.weather.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ConditionState
import com.andb.apps.weather.LocationState
import com.andb.apps.weather.ui.common.ProvideIsLoading
import com.andb.apps.weather.ui.common.placeholderOnLoading
import com.andb.apps.weather.ui.theme.onPrimarySecondary
import kotlin.math.roundToInt

data class UpperContentAnimationInfo(
    val scrollAmount: Dp,
    val upperContentHeight: Dp,
)

@Composable
fun UpperContent(
    locationState: LocationState.WithLocation,
    conditionState: ConditionState,
    animationInfo: UpperContentAnimationInfo,
    modifier: Modifier = Modifier,
    onOpenLocationPicker: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        LocationHeader(
            locationState = locationState,
            modifier = Modifier
                .padding(top = animationInfo.scrollAmount.coerceAtMost(animationInfo.upperContentHeight - 128.dp))
                .fillMaxWidth(),
            onClick = onOpenLocationPicker
        )
        Spacer(modifier = Modifier.weight(1f))
        when (conditionState) {
            is ConditionState.Error -> {}
            ConditionState.NotLoaded -> ProvideIsLoading(conditionState.isLoading) {
                CurrentConditions(temperature = 68.0, apparentTemperature = 68.0)
            }

            is ConditionState.Ok -> CurrentConditions(
                temperature = conditionState.resource.current.temperature,
                apparentTemperature = conditionState.resource.current.apparentTemperature
            )
        }

    }
}

@Composable
fun CurrentConditions(
    temperature: Double,
    apparentTemperature: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "${temperature.roundToInt()}°",
            style = MaterialTheme.typography.h1,
            color = MaterialTheme.colors.onPrimary,
            modifier = Modifier
                .placeholderOnLoading(shape = RoundedCornerShape(16.dp))
                .alignBy(LastBaseline),
        )
        Text(
            text = "Feels like ${apparentTemperature.roundToInt()}°",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onPrimarySecondary,
            modifier = Modifier
                .placeholderOnLoading()
                .alignBy(LastBaseline),
        )
    }
}
