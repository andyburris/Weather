package com.andb.apps.weather.data

fun Double.toFahrenheit(): Double = (this * (9.0 / 5.0)) + 32.0
fun Double.toCelsius(): Double = (this - 32) * (5.0 / 9.0)

fun Double.toMph(): Double = this / 1.609
fun Double.toKph(): Double = this * 1.609