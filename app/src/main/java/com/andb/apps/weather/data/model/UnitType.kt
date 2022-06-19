package com.andb.apps.weather.data.model

enum class UnitType(val string: String) {
    US("us"), SI("si");

    override fun toString(): String = string
}