package com.andb.apps.weather.ui.common

data class FastIntRange(val start: Int, val end: Int) {
    val first = start
    val last = end
    val size = end - start
}

fun FastIntRange.asIntRange() = start..end
infix fun Int.fastTo(end: Int) = FastIntRange(this, end)