package com.andb.apps.weather.util

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import com.andb.apps.weather.ConditionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random


fun secondsToLocalDateTime(seconds: Long): LocalDateTime {
    val instant = Instant.ofEpochSecond(seconds)
    return instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun statusBarHeight(resources: Resources): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return resources.getDimensionPixelSize(resourceId)
}

val IntRange.size
    get() = endInclusive - start

fun ConditionState?.isDaytime() = when (this) {
    is ConditionState.Ok -> this.resource.current.time in this.resource.days.first().day.let { it.sunriseTime..it.sunsetTime }
    else -> LocalTime.now() in LocalTime.of(6, 0)..LocalTime.of(19, 0)
}

fun randomColor(): Color = Color(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))

/**
 * Returns a [Flow] whose values are generated with [transform] function by combining
 * the most recently emitted values by each flow.
 */
@Suppress("UNCHECKED_CAST")
public fun <T1, T2, T3, T4, T5, T6, R> combine6(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = combine(flow, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
    transform(
        args[0] as T1,
        args[1] as T2,
        args[2] as T3,
        args[3] as T4,
        args[4] as T5,
        args[5] as T6,
    )
}