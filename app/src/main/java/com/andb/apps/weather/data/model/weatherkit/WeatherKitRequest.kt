package com.andb.apps.weather.data.model.weatherkit

import com.squareup.moshi.Json

data class WeatherKitRequest(
    @field:Json(name = "currentWeather") val currentWeather: WeatherKitCurrentConditions,
    @field:Json(name = "forecastNextHour") val forecastNextHour: WeatherKitNextHour,
    @field:Json(name = "forecastHourly") val forecastHourly: WeatherKitHourly,
    @field:Json(name = "forecastDaily") val forecastDaily: WeatherKitDaily
)

data class WeatherKitCurrentConditions(
    @field:Json(name = "name") val name: String,
    @field:Json(name = "metadata") val metadata: WeatherKitMetadata,
    @field:Json(name = "asOf") val asOf: String,
    @field:Json(name = "cloudCover") val cloudCover: Double,
    @field:Json(name = "cloudCoverLowAltPct") val cloudCoverLowAltPct: Double,
    @field:Json(name = "cloudCoverMidAltPct") val cloudCoverMidAltPct: Double,
    @field:Json(name = "cloudCoverHighAltPct") val cloudCoverHighAltPct: Double,
    @field:Json(name = "conditionCode") val conditionCode: WeatherKitConditionCode,
    @field:Json(name = "daylight") val daylight: Boolean,
    @field:Json(name = "humidity") val humidity: Double,
    @field:Json(name = "precipitationIntensity") val precipitationIntensity: Double,
    @field:Json(name = "pressure") val pressure: Double,
    @field:Json(name = "pressureTrend") val pressureTrend: String,
    @field:Json(name = "temperature") val temperature: Double,
    @field:Json(name = "temperatureApparent") val temperatureApparent: Double,
    @field:Json(name = "temperatureDewPoint") val temperatureDewPoint: Double,
    @field:Json(name = "uvIndex") val uvIndex: Int,
    @field:Json(name = "visibility") val visibility: Double,
    @field:Json(name = "windDirection") val windDirection: Int,
    @field:Json(name = "windGust") val windGust: Double,
    @field:Json(name = "windSpeed") val windSpeed: Double,
)

data class WeatherKitMetadata(
    @field:Json(name = "attributionURL") val attributionURL: String,
    @field:Json(name = "expireTime") val expireTime: String,
    @field:Json(name = "language") val language: String? = null,
    @field:Json(name = "latitude") val latitude: Double,
    @field:Json(name = "longitude") val longitude: Double,
    @field:Json(name = "readTime") val readTime: String,
    @field:Json(name = "reportedTime") val reportedTime: String? = null,
    @field:Json(name = "units") val units: String,
    @field:Json(name = "version") val version: String,
)

data class WeatherKitNextHour(
    val name: String,
    val metadata: WeatherKitMetadata,
    val summary: List<WeatherKitNextHourSummary>,
    val forecastStart: String,
    val forecastEnd: String,
    val minutes: List<WeatherKitMinuteConditions>
)

data class WeatherKitNextHourSummary(
    @field:Json(name = "startTime") val startTime: String,
    @field:Json(name = "condition") val condition: WeatherKitPrecipitationType,
    @field:Json(name = "precipitationChance") val precipitationChance: Double,
    @field:Json(name = "precipitationIntensity") val precipitationIntensity: Double,
)

data class WeatherKitMinuteConditions(
    @field:Json(name = "startTime") val startTime: String,
    @field:Json(name = "precipitationChance") val precipitationChance: Double,
    @field:Json(name = "precipitationIntensity") val precipitationIntensity: Double,
)

class WeatherKitHourly(
    val name: String,
    val metadata: WeatherKitMetadata,
    val hours: List<WeatherKitHourlyConditions>
)

data class WeatherKitHourlyConditions(
    @field:Json(name = "forecastStart") val forecastStart: String,
    @field:Json(name = "cloudCover") val cloudCover: Double,
    @field:Json(name = "conditionCode") val conditionCode: WeatherKitConditionCode,
    @field:Json(name = "daylight") val daylight: Boolean,
    @field:Json(name = "humidity") val humidity: Double,
    @field:Json(name = "precipitationAmount") val precipitationAmount: Double,
    @field:Json(name = "precipitationIntensity") val precipitationIntensity: Double,
    @field:Json(name = "precipitationChance") val precipitationChance: Double,
    @field:Json(name = "precipitationType") val precipitationType: WeatherKitPrecipitationType,
    @field:Json(name = "pressure") val pressure: Double,
    @field:Json(name = "pressureTrend") val pressureTrend: String,
    @field:Json(name = "snowfallIntensity") val snowfallIntensity: Double,
    @field:Json(name = "snowfallAmount") val snowfallAmount: Double,
    @field:Json(name = "temperature") val temperature: Double,
    @field:Json(name = "temperatureApparent") val temperatureApparent: Double,
    @field:Json(name = "temperatureDewPoint") val temperatureDewPoint: Double,
    @field:Json(name = "uvIndex") val uvIndex: Int,
    @field:Json(name = "visibility") val visibility: Double,
    @field:Json(name = "windDirection") val windDirection: Int,
    @field:Json(name = "windGust") val windGust: Double,
    @field:Json(name = "windSpeed") val windSpeed: Double,
)

data class WeatherKitDaily(
    val name: String,
    val metadata: WeatherKitMetadata,
    val days: List<WeatherKitDailyConditions>
)

data class WeatherKitDailyConditions(
    @field:Json(name = "forecastStart") val forecastStart: String,
    @field:Json(name = "forecastEnd") val forecastEnd: String,
    @field:Json(name = "conditionCode") val conditionCode: WeatherKitConditionCode,
    @field:Json(name = "maxUvIndex") val maxUvIndex: Int,
    @field:Json(name = "moonPhase") val moonPhase: WeatherKitMoonPhase,
    @field:Json(name = "moonrise") val moonrise: String? = null,
    @field:Json(name = "moonset") val moonset: String,
    @field:Json(name = "precipitationAmount") val precipitationAmount: Double,
    @field:Json(name = "precipitationAmountByType") val precipitationAmountByType: WeatherKitPrecipitationAmountByType,
    @field:Json(name = "precipitationChance") val precipitationChance: Double,
    @field:Json(name = "precipitationType") val precipitationType: WeatherKitPrecipitationType,
    @field:Json(name = "snowfallAmount") val snowfallAmount: Double,
    @field:Json(name = "solarMidnight") val solarMidnight: String,
    @field:Json(name = "solarNoon") val solarNoon: String,
    @field:Json(name = "sunrise") val sunrise: String,
    @field:Json(name = "sunriseCivil") val sunriseCivil: String,
    @field:Json(name = "sunriseNautical") val sunriseNautical: String,
    @field:Json(name = "sunriseAstronomical") val sunriseAstronomical: String,
    @field:Json(name = "sunset") val sunset: String,
    @field:Json(name = "sunsetCivil") val sunsetCivil: String,
    @field:Json(name = "sunsetNautical") val sunsetNautical: String,
    @field:Json(name = "sunsetAstronomical") val sunsetAstronomical: String,
    @field:Json(name = "temperatureMax") val temperatureMax: Double,
    @field:Json(name = "temperatureMin") val temperatureMin: Double,
    @field:Json(name = "daytimeForecast") val daytimeForecast: WeatherKitDayPart? = null,
    @field:Json(name = "overnightForecast") val overnightForecast: WeatherKitDayPart? = null,
    @field:Json(name = "restOfDayForecast") val restOfDayForecast: WeatherKitDayPart? = null,
)

data class WeatherKitDayPart(
    @field:Json(name = "forecastStart") val forecastStart: String,
    @field:Json(name = "forecastEnd") val forecastEnd: String,
    @field:Json(name = "cloudCover") val cloudCover: Double,
    @field:Json(name = "conditionCode") val conditionCode: WeatherKitConditionCode,
    @field:Json(name = "humidity") val humidity: Double,
    @field:Json(name = "precipitationAmount") val precipitationAmount: Double,
    @field:Json(name = "precipitationAmountByType") val precipitationAmountByType: WeatherKitPrecipitationAmountByType,
    @field:Json(name = "precipitationChance") val precipitationChance: Double,
    @field:Json(name = "precipitationType") val precipitationType: WeatherKitPrecipitationType,
    @field:Json(name = "snowfallAmount") val snowfallAmount: Double,
    @field:Json(name = "windDirection") val windDirection: Int,
    @field:Json(name = "windSpeed") val windSpeed: Double,
)

data class WeatherKitPrecipitationAmountByType(
    @field:Json(name = "rain") val rain: Double = 0.0,
    @field:Json(name = "snow") val snow: Double = 0.0,
)

enum class WeatherKitConditionCode {
    Clear,
    Cloudy,
    Dust,
    Fog,
    Haze,
    MostlyClear,
    MostlyCloudy,
    PartlyCloudy,
    ScatteredThunderstorms,
    Smoke,
    Breezy,
    Windy,
    Drizzle,
    HeavyRain,
    Rain,
    Showers,
    Flurries,
    HeavySnow,
    MixedRainAndSleet,
    MixedRainAndSnow,
    MixedRainfall,
    MixedSnowAndSleet,
    ScatteredShowers,
    ScatteredSnowShowers,
    Sleet,
    Snow,
    SnowShowers,
    Blizzard,
    BlowingSnow,
    FreezingDrizzle,
    FreezingRain,
    Frigid,
    Hail,
    Hot,
    Hurricane,
    IsolatedThunderstorms,
    Thunderstorms,
    Tornado,
    TropicalStorm,
}

enum class WeatherKitPrecipitationType {
    clear, precipitation, rain, snow, sleet, hail, mixed
}

enum class WeatherKitMoonPhase {
    /**The moon isn’t visible.**/
    new,

    /**A crescent-shaped sliver of the moon is visible, and increasing in size.**/
    waxingCrescent,

    /**Approximately half of the moon is visible, and increasing in size.**/
    firstQuarter,

    /**The entire disc of the moon is visible.**/
    full,

    /**More than half of the moon is visible, and increasing in size.**/
    waxingGibbous,

    /**More than half of the moon is visible, and decreasing in size.**/
    waningGibbous,

    /**Approximately half of the moon is visible, and decreasing in size.**/
    thirdQuarter,

    /**A crescent-shaped sliver of the moon is visible, and decreasing in size.**/
    waningCrescent,

}