package com.andb.apps.weather.data.repository.weather

import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.data.model.weatherkit.toConditions
import com.andb.apps.weather.data.remote.DataSets
import com.andb.apps.weather.data.remote.WeatherKitService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.util.decodeBase64Bytes
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

data class WeatherKitConfig(
    val keyID: String,
    val teamID: String,
    val serviceID: String,
    val privateKey: String,
)

data class WeatherKitRepo(
    val weatherKitService: WeatherKitService,
    val config: WeatherKitConfig,
) : ProviderRepo {
    override suspend fun getConditions(lat: Double, long: Double): Result<Conditions> {
        config.generateJWT()
        return runCatching {
            val token = config.generateJWT()
            val result = weatherKitService.getForecast(
                "en-US",
                lat,
                long,
                listOf(
                    DataSets.currentWeather,
                    DataSets.forecastHourly,
                    DataSets.forecastDaily,
                    DataSets.forecastNextHour
                ).joinToString(",") { it.name },
                "Bearer $token"
            )
            return@runCatching result.toConditions()
        }
    }
}

private fun WeatherKitConfig.generateJWT() = JWT.create()
    .withKeyId(this.keyID)
    .withHeader(
        mapOf(
            "id" to "${this.teamID}.${this.serviceID}",
        )
    )
    .withIssuer(this.teamID)
    .withIssuedAt(Date.from(Instant.now()))
    .withExpiresAt(
        Date.from(
            Instant.now().plus(30, ChronoUnit.MINUTES)
        )
    )
    .withSubject(this.serviceID)
    .sign(Algorithm.ECDSA256(this.privateKey.toPrivateKey()))

fun String.toPrivateKey(): ECPrivateKey {
    val kf = KeyFactory.getInstance("EC")
    val bytes = decodeBase64Bytes()
    return kf.generatePrivate(PKCS8EncodedKeySpec(bytes)) as ECPrivateKey
}