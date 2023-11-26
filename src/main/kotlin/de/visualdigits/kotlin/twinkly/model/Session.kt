package de.visualdigits.kotlin.twinkly.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.common.networkstatus.NetworkStatus
import de.visualdigits.kotlin.twinkly.model.xled.response.ResponseCode
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
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

    protected var authToken: String? = null

    fun login() {
        val challenge = Base64.getEncoder().encode(Random(System.currentTimeMillis()).nextBytes(32)).decodeToString()
        log.info("#### Logging into device at $host...")
        val responseChallenge = post<Map<String, Any>>(
            url = "$baseUrl/login",
            body = "{\"challenge\":\"$challenge\"}".toByteArray(),
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )
        authToken = responseChallenge["authentication_token"]!! as String
        val responseVerify = verify(responseChallenge["challenge-response"]!! as String)
        if (responseVerify.responseCode != ResponseCode.Ok) throw IllegalStateException("Could not login to device")
    }

    private fun verify(responseChallenge: String): JsonObject {
        return post<JsonObject>(
            url = "$baseUrl/verify",
            body = "{\"challenge_response\":\"${responseChallenge}\"}".toByteArray(),
            headers = mapOf(
                "Content-Type" to "application/json"
            )
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

    protected inline fun <reified T> post(url: String, body: ByteArray = byteArrayOf(), headers: Map<String, String> = mapOf()): T {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "POST"
        authToken?.let { at -> connection.setRequestProperty("X-Auth-Token", at) }
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
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

    protected inline fun <reified T> get(url: String, headers: Map<String, String> = mapOf()): T {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "GET"
        authToken?.let { at -> connection.setRequestProperty("X-Auth-Token", at) }
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

    protected inline fun <reified T> delete(url: String, headers: Map<String, String> = mapOf()): T {
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "DELETE"
        authToken?.let { at -> connection.setRequestProperty("X-Auth-Token", at) }
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
}