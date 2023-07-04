package com.andb.apps.weather.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.data.model.HourlyConditions
import com.andb.apps.weather.ui.theme.onBackgroundSecondary
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

private fun <T> animationSpec() = tween<T>()

private val GraphBarShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)

@OptIn(ExperimentalTime::class)
@Composable
fun HourlyItem(
    selectedView: HomeView,
    hourlyConditions: HourlyConditions,
    globalRanges: GlobalRanges,
    barColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
//        modifier = modifier.recomposeHighlighter(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Bottom),
    ) {
        val barData = remember(hourlyConditions, selectedView, globalRanges) {
            hourlyConditions.graphBarData(homeView = selectedView, globalRanges = globalRanges)
        }
        val barHeightAnimated = animateFloatAsState(
            targetValue = barData.percent.toFloat(),
            animationSpec = animationSpec()
        )
        val barColorAnimated = animateColorAsState(
            targetValue = barColor,
            animationSpec = animationSpec()
        )
        Label(label = barData.label, decoration = barData.decoration)
        Box(modifier = Modifier.weight(1f, fill = false)) {
            Box(
                modifier = Modifier
                    .background(
                        color = barColorAnimated.value,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
                    .heightIn(min = 2.dp)
                    .fillMaxHeight(fraction = barHeightAnimated.value)
                    .width(32.dp)
            )
        }
        Time(timeText = barData.timeText)
    }
}

@Composable
private fun Label(
    label: String,
    decoration: String?,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
        }
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colors.onBackground)) {
                    append(label)
                }
                if (decoration != null) {
                    withStyle(SpanStyle(color = MaterialTheme.colors.onBackgroundSecondary)) {
                        append(decoration)
                    }
                }
            },
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onBackground,
        )
    }
}

private val formatter = DateTimeFormatter.ofPattern("ha")

@Composable
private fun Time(timeText: String, modifier: Modifier = Modifier) {
    Text(
        text = timeText,
        style = MaterialTheme.typography.caption,
        color = MaterialTheme.colors.onBackgroundSecondary,
        modifier = modifier,
    )
}

private data class GraphBarData(
    val percent: Double,
    val label: String,
    val decoration: String? = null,
    val timeText: String,
)

private fun HourlyConditions.graphBarData(
    homeView: HomeView,
    globalRanges: GlobalRanges
) = when (homeView) {
    HomeView.Summary -> throw Error("Should never display a graph for Summary")
    HomeView.Temperature -> GraphBarData(
        percent = (this.temperature - globalRanges.temperatureRange.first) / globalRanges.temperatureRange.size,
        label = this@graphBarData.temperature.roundToInt().toString(),
        decoration = "°",
        timeText = formatter.format(time),
    )

    HomeView.Rain -> GraphBarData(
        percent = this.precipProbability,
        label = (this@graphBarData.precipProbability * 100).roundToInt().toString(),
        decoration = "%",
        timeText = formatter.format(time),
    )
        HomeView.UV -> GraphBarData(
            percent = this.uvIndex / 10.0,
            label = this.uvIndex.toString(),
            timeText = formatter.format(time),
        )
        HomeView.Wind -> GraphBarData(
            percent = ((this.windSpeed - globalRanges.windRange.first) / globalRanges.windRange.size.toDouble()).also {
                println("wind percent = $it, globalRanges.windRange = ${globalRanges.windRange}, this.windSpeed = ${this.windSpeed}")
            },
            label = this@graphBarData.windSpeed.roundToInt().toString(),
            decoration = "mph",
            timeText = formatter.format(time),
        )
    }