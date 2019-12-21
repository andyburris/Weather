package com.andb.apps.weather.util

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import kotlin.math.roundToInt


fun newIoThread(block: suspend CoroutineScope.() -> Unit): Job {
    return CoroutineScope(Dispatchers.IO).launch(block = block)
}

suspend fun mainThread(block: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.Main, block)
}

suspend fun ioThread(block: suspend CoroutineScope.() -> Unit) {
    withContext(Dispatchers.IO, block)
}

fun <E, R : Comparable<R>> Collection<E>.mapMax(block: (E) -> R): R? {
    return this.map(block).maxBy { it }
}

fun Context.getColorCompat(colorRes: Int): Int {
    return ContextCompat.getColor(this, colorRes)
}

fun secondsToLocalDateTime(seconds: Long): LocalDateTime {
    val instant = Instant.ofEpochSecond(seconds)
    return instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun <E> MutableList<E>.removeRange(start: Int, end: Int) {
    removeAll(subList(start, end))
}

fun leastCommonMultiple(n1: Int, n2: Int): Int {
    return (n1 * n2) / greatestCommonDenominator(n1, n2)
}

fun greatestCommonDenominator(n1: Int, n2: Int): Int {
    var gcd = 1
    var i = 1
    while (i <= n1 && i <= n2) {
        // Checks if i is factor of both integers
        if (n1 % i == 0 && n2 % i == 0)
            gcd = i
        ++i
    }
    return gcd
}

fun colorBetween(cStart: Int, cEnd: Int, amount: Float): Int {

    val r1 = Color.red(cStart).toFloat()
    val g1 = Color.green(cStart).toFloat()
    val b1 = Color.blue(cStart).toFloat()

    val r2 = Color.red(cEnd).toFloat()
    val g2 = Color.green(cEnd).toFloat()
    val b2 = Color.blue(cEnd).toFloat()

    val r = transposeRange(0f, 1f, r1, r2, amount).roundToInt()
    val g = transposeRange(0f, 1f, g1, g2, amount).roundToInt()
    val b = transposeRange(0f, 1f, b1, b2, amount).roundToInt()

    //System.out.println("colorBetween - r1: " + r1 + ", g1: " + g1 + ", b1: " + b1 + ", r2: " + r2 + ", g2: " + g2 + ", b2: " + b2 + ", r: " + r + ", g: " + g + ", b: " + b);

    return Color.rgb(r, g, b)
}

fun transposeRange(
    oldMin: Float,
    oldMax: Float,
    newMin: Float,
    newMax: Float,
    oldValue: Float
): Float {
    val oldRange = oldMax - oldMin
    val newRange = newMax - newMin
    return (oldValue - oldMin) * newRange / oldRange + newMin
}

fun dpToPx(dp: Int): Int {
    val scale = Resources.getSystem().displayMetrics.density
    return (dp * scale).toInt()
}

val Int.dp
    get() = dpToPx(this)

fun statusBarHeight(resources: Resources): Int {
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return resources.getDimensionPixelSize(resourceId)
}

fun chipTextFrom(color: Int): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)

    Log.d("chipTextFrom", "original hsv: [${hsv[0]}, ${hsv[1]}, ${hsv[2]}]")

    when {
        hsv[2] >= .60 -> hsv[2] = .10f
        hsv[2] < .60 -> hsv[2] = .90f
    }

    Log.d("chipTextFrom", "new hsv: [${hsv[0]}, ${hsv[1]}, ${hsv[2]}]")

    val newColor = Color.HSVToColor(hsv)

    Log.d(
        "chipTextFrom",
        "original color: #${color.toHexString()}, new color: #${newColor.toHexString()}"
    )

    return newColor
}

fun Int.toHexString(): String {
    return Integer.toHexString(this)
}

fun createChipStateList(
    checkedColor: Int,
    backgroundColor: Int = Color.parseColor("#00FFFFFF")
): ColorStateList {
    val states = arrayOf(
        intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected), // checked
        intArrayOf(android.R.attr.state_enabled) // default
    )

    val colors = intArrayOf(checkedColor, backgroundColor)

    return ColorStateList(states, colors)
}

fun <T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, block: (T) -> Unit) {
    this.observe(lifecycleOwner, Observer(block))
}

fun roundNearest(value: Int, increment: Int): Int {
    val mod = value % increment
    return when (mod) {
        0 -> value
        in 0..(increment / 2) -> value - mod
        else -> value - mod + increment
    }
}

fun Int.mapRange(currentRange: IntRange, newRange: IntRange) =
    (((this - currentRange.first) * newRange.size) / currentRange.size) + 100

val IntRange.size
    get() = endInclusive - start

fun RecyclerView.LayoutManager.forEach(block: (View?) -> Unit) {
    for (i in 0..childCount) {
        block.invoke(getChildAt(i))
    }
}

fun Resources.colorByNightMode(colorDay: Int, colorNight: Int): Int {
    val nightMode = this.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return when (nightMode) {
        Configuration.UI_MODE_NIGHT_NO -> colorDay
        Configuration.UI_MODE_NIGHT_YES -> colorNight
        else -> colorDay
    }
}

fun Context.colorByNightModeRes(colorDayRes: Int, colorNightRes: Int): Int {
    val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return when (nightMode) {
        Configuration.UI_MODE_NIGHT_NO -> ContextCompat.getColor(this, colorDayRes)
        Configuration.UI_MODE_NIGHT_YES -> ContextCompat.getColor(this, colorNightRes)
        else -> ContextCompat.getColor(this, colorDayRes)
    }
}

fun Fragment.colorByNightMode(colorDay: Int, colorNight: Int) =
    resources.colorByNightMode(colorDay, colorNight)

fun Fragment.colorByNightModeRes(colorDayRes: Int, colorNightRes: Int) =
    context?.colorByNightModeRes(colorDayRes, colorNightRes)

fun Activity.colorByNightMode(colorDay: Int, colorNight: Int) =
    resources.colorByNightMode(colorDay, colorNight)

fun <T> MutableList<T>.reset(with: Collection<T>) {
    this.clear()
    this.addAll(with)
}