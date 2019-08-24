package com.andb.apps.weather

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
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

fun transposeRange(oldMin: Float, oldMax: Float, newMin: Float, newMax: Float, oldValue: Float): Float {
    val oldRange = oldMax - oldMin
    val newRange = newMax - newMin
    return (oldValue - oldMin) * newRange / oldRange + newMin
}