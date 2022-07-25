package com.andb.apps.weather.util

import android.content.res.Resources
import com.andb.apps.weather.ConditionState
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


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