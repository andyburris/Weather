package com.andb.apps.weather.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.data.model.HourlyConditions
import com.andb.apps.weather.ui.theme.WeatherColors
import com.andb.apps.weather.ui.theme.onBackgroundSecondary
import com.andb.apps.weather.util.size
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private fun <T> animationSpec() = tween<T>()

@Composable
fun HourlyItem(
    selectedView: HomeView,
    hourlyConditions: HourlyConditions,
    globalRanges: GlobalRanges,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.Bottom),
    ) {
        val barData =
            hourlyConditions.graphBarData(homeView = selectedView, globalRanges = globalRanges)
        val barHeight = animateFloatAsState(
            targetValue = barData.percent.toFloat(),
            animationSpec = animationSpec()
        )
        val barColor = animateColorAsState(
            targetValue = WeatherColors.viewColors(selectedView).graphic,
            animationSpec = animationSpec()
        )
        Label(text = barData.label)
        Box(modifier = Modifier.weight(1f, fill = false)) {
            Box(
                modifier = Modifier
                    .background(
                        color = barColor.value,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
                    .heightIn(min = 2.dp)
                    .fillMaxHeight(fraction = barHeight.value)
                    .width(32.dp)
            )
        }
        Time(time = hourlyConditions.time)
    }
}

@Composable
private fun Label(
    text: AnnotatedString,
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
            text = text,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onBackground,
        )
    }
}

@Composable
private fun Time(time: ZonedDateTime, modifier: Modifier = Modifier) {
    Text(
        text = time.format(DateTimeFormatter.ofPattern("ha")),
        style = MaterialTheme.typography.caption,
        color = MaterialTheme.colors.onBackgroundSecondary,
        modifier = modifier,
    )
}

private data class GraphBarData(
    val percent: Double,
    val label: AnnotatedString,
)

@Composable
private fun HourlyConditions.graphBarData(homeView: HomeView, globalRanges: GlobalRanges) =
    when (homeView) {
        HomeView.Summary -> throw Error("Should never display a graph for Summary")
        HomeView.Temperature -> GraphBarData(
            percent = (this.temperature - globalRanges.temperatureRange.first) / globalRanges.temperatureRange.size,
            label = buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colors.onBackground)) {
                    append(this@graphBarData.temperature.roundToInt().toString())
                }
                withStyle(SpanStyle(color = MaterialTheme.colors.onBackgroundSecondary)) {
                    append("°")
                }
            }
        )
        HomeView.Rain -> GraphBarData(
            percent = this.precipProbability,
            label = buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colors.onBackground)) {
                    append((this@graphBarData.precipProbability * 100).roundToInt().toString())
                }
                withStyle(SpanStyle(color = MaterialTheme.colors.onBackgroundSecondary)) {
                    append("%")
                }
            }
        )
        HomeView.UV -> GraphBarData(
            percent = this.uvIndex / 10.0,
            label = AnnotatedString(this.uvIndex.toString())
        )
        HomeView.Wind -> GraphBarData(
            percent = ((this.windSpeed - globalRanges.windRange.first) / globalRanges.windRange.size.toDouble()).also {
                println("wind percent = $it, globalRanges.windRange = ${globalRanges.windRange}, this.windSpeed = ${this.windSpeed}")
            },
            label = buildAnnotatedString {
                withStyle(SpanStyle(color = MaterialTheme.colors.onBackground)) {
                    append(this@graphBarData.windSpeed.roundToInt().toString())
                }
                withStyle(SpanStyle(color = MaterialTheme.colors.onBackgroundSecondary)) {
                    append("mph")
                }
            }
        )
    }