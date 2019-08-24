package com.andb.apps.weather

import com.andb.apps.weather.objects.CurrentConditions
import com.andb.apps.weather.objects.Daily
import com.andb.apps.weather.objects.Hourly
import com.andb.apps.weather.objects.Minutely
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.ToJson
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset

class DarkSkyRequest(
    @field:Json(name = "latitude") val latitude: Float,
    @field:Json(name = "longitude") val longitude: Float,
    @field:Json(name = "timezone") val timezone: ZoneOffset,
    @field:Json(name = "currently") val currently: CurrentConditions,
    @field:Json(name = "minutely") val minutely: Minutely,
    @field:Json(name = "hourly") val hourly: Hourly,
    @field:Json(name = "daily") val daily: Daily
)

class ZoneOffsetAdapter {
    @ToJson
    fun toJson(zo: ZoneOffset): String? {
        return zo.id
    }

    @FromJson
    fun fromJson(string: String): ZoneOffset {
        return ZoneId.of(string).rules.getOffset(LocalDateTime.now())
    }
}