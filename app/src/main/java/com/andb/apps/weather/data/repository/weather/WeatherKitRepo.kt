package com.andb.apps.weather.data.repository.weather

import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.data.model.weatherkit.toConditions
import com.andb.apps.weather.data.remote.WeatherKitService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date

data class WeatherKitConfig(
    val keyID: String,
    val teamID: String,
    val serviceID: String,
    val keys: Pair<ECPublicKey, ECPrivateKey>,
)

data class WeatherKitRepo(
    val weatherKitService: WeatherKitService,
    val config: WeatherKitConfig,
) : ProviderRepo {
    override suspend fun getConditions(lat: Double, long: Double): Result<Conditions> =
        runCatching {
            val token = JWT.create()
                .withKeyId(config.keyID)
                .withHeader(mapOf("id" to "${config.teamID}.${config.serviceID}"))
                .withIssuer(config.teamID)
                .withIssuedAt(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)))
                .withExpiresAt(
                    Date.from(
                        LocalDateTime.now().plusMinutes(30).toInstant(ZoneOffset.UTC)
                    )
                )
                .withSubject(config.serviceID)
                .sign(Algorithm.ECDSA256(config.keys.first, config.keys.second))
            val result = weatherKitService.getForecast("en-US", lat, long, token)
            return@runCatching result.toConditions()
        }
}