package de.visualdigits.kotlin.twinkly.model.device

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.common.networkstatus.NetworkStatus
import de.visualdigits.kotlin.twinkly.model.device.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Base64
import kotlin.random.Random

const val UDP_PORT = 5555
const val UDP_PORT_MUSIC = 5556
const val UDP_PORT_STREAMING = 7777

const val CONNECTION_TIMEOUT = 5000

abstract class Session(
    val host: String,
    val baseUrl: String
) {

    protected val log = LoggerFactory.getLogger(javaClass)

    protected val mapper = jacksonObjectMapper()

    companion object {
        val tokens: MutableMap<String, AuthToken> = mutableMapOf()
    }

    fun login() {
        if (!isLoggedIn()) { // avoid additional attempts from other instances if we already know that we cannot talk to the host
            log.debug("#### Logging into device at $host...")
            val challenge = Base64.getEncoder().encode(Random(System.currentTimeMillis()).nextBytes(32)).decodeToString()
            val responseChallenge = post<Map<String, Any>>(
                url = "$baseUrl/login",
                body = "{\"challenge\":\"$challenge\"}".toByteArray(),
                headers = mutableMapOf(
                    "Content-Type" to "application/json"
                )
            )
            if (responseChallenge != null) {
                val authToken = responseChallenge["authentication_token"] as String
                val expireInSeconds = (responseChallenge["authentication_token_expires_in"] as Int)
                val responseVerify = verify((responseChallenge["challenge-response"] as String), authToken)
                if (responseVerify?.responseCode != ResponseCode.Ok) {
                    log.warn("Could not login to device")
                }
                val tokenExpires = System.currentTimeMillis() + expireInSeconds * 1000 - 5000
                log.debug("#### Token expires '${formatEpoch(tokenExpires)}'")
                tokens[host] = AuthToken(authToken, tokenExpires, true)
            } else {
                tokens[host] = AuthToken(loggedIn = false)
            }
        }
   }

    fun refreshTokenIfNeeded() {
        if (System.currentTimeMillis() > (tokens[host]?.tokenExpires?:0)) {
            log.debug("Refreshing token for device '$host'...")
            tokens.remove(host)
            login()
        }
    }

    private fun verify(responseChallenge: String, authToken: String): JsonObject? {
        return post<JsonObject>(
            url = "$baseUrl/verify",
            body = "{\"challenge_response\":\"${responseChallenge}\"}".toByteArray(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            ),
            authToken = authToken
        )
    }

    fun isLoggedIn(): Boolean = tokens.containsKey(host)

    open fun logout() {
        post<JsonObject>("$baseUrl/logout")
    }

    fun deviceInfo(): DeviceInfo? {
        return get<DeviceInfo>("$baseUrl/gestalt")
    }

    fun status(): JsonObject? {
        return get<JsonObject>(
            url = "$baseUrl/status",
        )
    }

    fun networkStatus(): NetworkStatus? {
        return get<NetworkStatus>(
            url = "$baseUrl/network/status",
        )
    }

    protected inline fun <reified T> post(
        url: String,
        body: ByteArray = byteArrayOf(),
        headers: MutableMap<String, String> = mutableMapOf(),
        authToken: String? = null
    ): T? {
        log.debug("POST '$url' body='${String(body)}' headers=$headers")
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "POST"
        connection.connectTimeout = CONNECTION_TIMEOUT
        (authToken?:tokens[host]?.authToken)?.let { at -> headers["X-Auth-Token"] = at }
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        connection.doOutput = true
        return try {
            connection.outputStream.use { os ->
                os.write(body, 0, body.size)
            }
            val response = connection.inputStream.use { ins ->
                ins.readAllBytes()
            }
            when (T::class) {
                String::class -> String(response) as T
                else -> mapper.readValue(response, T::class.java)
            }
        } catch (e: Exception) {
            log.warn("Could not talk to server")
            null
        }
    }

    protected inline fun <reified T> get(
        url: String,
        headers: Map<String, String> = mapOf()
    ): T? {
        log.debug("GET '$url' headers=$headers")
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "GET"
        connection.connectTimeout = CONNECTION_TIMEOUT
        tokens[host]?.authToken?.let { at -> connection.setRequestProperty("X-Auth-Token", at) }
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        return try {
            val response = connection.inputStream.use { ins ->
                ins.readAllBytes()
            }
            when (T::class) {
                String::class -> String(response) as T
                else -> mapper.readValue(response, T::class.java)
            }
        } catch (e: Exception) {
            log.warn("Could not talk to server")
            null
        }
    }

    protected inline fun <reified T> delete(
        url: String,
        headers: Map<String, String> = mapOf()
    ): T? {
        log.debug("DELETE '$url' headers=$headers")
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "DELETE"
        connection.connectTimeout = CONNECTION_TIMEOUT
        tokens[host]?.authToken?.let { at -> connection.setRequestProperty("X-Auth-Token", at) }
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        return try {
            val response = connection.inputStream.use { ins ->
                ins.readAllBytes()
            }
            when (T::class) {
                String::class -> String(response) as T
                else -> mapper.readValue(response, T::class.java)
            }
        } catch (e: Exception) {
            log.warn("Could not talk to server")
            null
        }
    }

    protected fun formatEpoch(epoch: Long): String {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(epoch),
                ZoneId.systemDefault()
            )
        )
    }
}
