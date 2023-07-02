package com.andb.apps.weather.data.repository.weather

import com.andb.apps.weather.data.model.Conditions
import com.andb.apps.weather.data.model.weatherkit.toConditions
import com.andb.apps.weather.data.remote.DataSets
import com.andb.apps.weather.data.remote.WeatherKitService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.philjay.jwt.Base64Decoder
import com.philjay.jwt.Base64Encoder
import com.philjay.jwt.JWTAuthHeader
import com.philjay.jwt.JWTAuthPayload
import com.philjay.jwt.JsonEncoder
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
        config.generateJWT2()
        return runCatching {
            val token = config.generateJWT2()
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

private fun WeatherKitConfig.generateJWT2() = JWT.create()
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

class WeatherKitJWTPayload(
    iss: String,
    iat: Long,
    val exp: Long,
    val sub: String,
) : JWTAuthPayload(iss, iat)

class WeatherKitJWTHeader(
    alg: String = com.philjay.jwt.Algorithm.ES256.name,
    val kid: String,
    val id: String,
) : JWTAuthHeader(alg)

private suspend fun WeatherKitConfig.generateJWT(): String {
    val jsonEncoder: JsonEncoder<WeatherKitJWTHeader, WeatherKitJWTPayload> =
        object : JsonEncoder<WeatherKitJWTHeader, WeatherKitJWTPayload> {
            override fun toJson(header: WeatherKitJWTHeader): String = Json.encodeToString(
                mapOf(
                    "alg" to header.alg,
                    "kid" to header.kid,
                    "id" to header.id
                ).toJsonObject()
            )

            override fun toJson(payload: WeatherKitJWTPayload): String = Json.encodeToString(
                mapOf(
                    "iss" to payload.iss,
                    "iat" to payload.iat,
                    "exp" to payload.exp,
                    "sub" to payload.sub
                ).toJsonObject()
            )
        }
    val encoder = object : Base64Encoder {
        override fun encode(bytes: ByteArray): String = bytes.encodeBase64()
        override fun encodeURLSafe(bytes: ByteArray): String = bytes.encodeBase64()
    }
    val decoder = object : Base64Decoder {
        override fun decode(bytes: ByteArray): ByteArray =
            bytes.decodeToString().decodeBase64Bytes()

        override fun decode(string: String): ByteArray = string.decodeBase64Bytes()
    }
    return com.philjay.jwt.JWT.token(
        algorithm = com.philjay.jwt.Algorithm.ES256,
        header = WeatherKitJWTHeader(kid = keyID, id = "${this.teamID}.${this.serviceID}"),
        payload = WeatherKitJWTPayload(
            iss = teamID,
            iat = Instant.now().epochSecond,
            exp = Instant.now().plus(30, ChronoUnit.MINUTES).epochSecond,
            sub = serviceID,
        ),
        secret = privateKey,
        jsonEncoder = jsonEncoder,
        encoder = encoder,
        decoder = decoder,
    )
}

private fun Map<String, Any>.toJsonObject(): JsonObject = JsonObject(this.mapValues { (_, value) ->
    when (value) {
        is String -> JsonPrimitive(value)
        is Long -> JsonPrimitive(value)
        else -> throw Error("unsupported type")
    }
})

fun String.toPrivateKey(): ECPrivateKey {
    val kf = KeyFactory.getInstance("EC")
    val bytes = decodeBase64Bytes()
    return kf.generatePrivate(PKCS8EncodedKeySpec(bytes)) as ECPrivateKey
}