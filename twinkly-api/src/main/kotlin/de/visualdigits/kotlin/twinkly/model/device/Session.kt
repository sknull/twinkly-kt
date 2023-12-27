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
import java.util.*
import kotlin.random.Random

const val UDP_PORT = 5555
const val UDP_PORT_MUSIC = 5556
const val UDP_PORT_STREAMING = 7777

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
        if (!tokens.containsKey(host)) {
            val challenge = Base64.getEncoder().encode(Random(System.currentTimeMillis()).nextBytes(32)).decodeToString()
            log.info("#### Logging into device at $host...")
            val responseChallenge = post<Map<String, Any>>(
                url = "$baseUrl/login",
                body = "{\"challenge\":\"$challenge\"}".toByteArray(),
                headers = mutableMapOf(
                    "Content-Type" to "application/json"
                )
            )
            val authToken = responseChallenge["authentication_token"]!! as String
            val expireInSeconds = responseChallenge["authentication_token_expires_in"]!! as Int
            val responseVerify = verify(responseChallenge["challenge-response"]!! as String, authToken)
            if (responseVerify.responseCode != ResponseCode.Ok) throw IllegalStateException("Could not login to device")
            val tokenExpires = System.currentTimeMillis() + expireInSeconds * 1000 - 5000
            log.info("#### Token expires '${formatEpoch(tokenExpires)}'")
            tokens[host] = AuthToken(authToken, tokenExpires)
        }
   }

    fun refreshTokenIfNeeded() {
        if (System.currentTimeMillis() > (tokens[host]?.tokenExpires?:0)) {
            log.info("Refreshing token for device '$host'...")
            login()
        }
    }

    private fun verify(responseChallenge: String, authToken: String): JsonObject {
        return post<JsonObject>(
            url = "$baseUrl/verify",
            body = "{\"challenge_response\":\"${responseChallenge}\"}".toByteArray(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            ),
            authToken = authToken
        )
    }

    open fun logout() {
        post<JsonObject>("$baseUrl/logout")
    }

    fun deviceInfo(): DeviceInfo {
        return get<DeviceInfo>("$baseUrl/gestalt")
    }

    fun status(): JsonObject {
        return get<JsonObject>(
            url = "$baseUrl/status",
        )
    }

    fun networkStatus(): NetworkStatus {
        return get<NetworkStatus>(
            url = "$baseUrl/network/status",
        )
    }

    protected inline fun <reified T> post(
        url: String,
        body: ByteArray = byteArrayOf(),
        headers: MutableMap<String, String> = mutableMapOf(),
        authToken: String? = null
    ): T {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "POST"
        (authToken?:tokens[host]?.authToken)?.let { at -> headers["X-Auth-Token"] = at }
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        log.info("post '$url' body='${String(body)}' headers=$headers")
        connection.doOutput = true
        connection.outputStream.use { os ->
            os.write(body, 0, body.size)
        }
        val response = try {
            connection.inputStream.use { ins ->
                ins.readAllBytes()
            }
        } catch (e: Exception) {
            log.error("Could not retrieve response from server", e)
            throw e
        }
        return when (T::class) {
            String::class -> String(response) as T
            else -> mapper.readValue(response, T::class.java)
        }
    }

    protected inline fun <reified T> get(
        url: String,
        headers: Map<String, String> = mapOf()
    ): T {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "GET"
        tokens[host]?.authToken?.let { at -> connection.setRequestProperty("X-Auth-Token", at) }
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        val response = try {
            connection.inputStream.use { ins ->
                ins.readAllBytes()
            }
        } catch (e: Exception) {
            log.error("Could not retrieve response from server", e)
            throw e
        }
        return when (T::class) {
            String::class -> String(response) as T
            else -> mapper.readValue(response, T::class.java)
        }
    }

    protected inline fun <reified T> delete(
        url: String,
        headers: Map<String, String> = mapOf()
    ): T {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "DELETE"
        tokens[host]?.authToken?.let { at -> connection.setRequestProperty("X-Auth-Token", at) }
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        val response = try {
            connection.inputStream.use { ins ->
                ins.readAllBytes()
            }
        } catch (e: Exception) {
            log.error("Could not retrieve response from server", e)
            throw e
        }
        return when (T::class) {
            String::class -> String(response) as T
            else -> mapper.readValue(response, T::class.java)
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
