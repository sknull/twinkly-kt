package de.visualdigits.kotlin.twinkly.model.device

import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.common.networkstatus.NetworkStatus
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import de.visualdigits.kotlin.util.delete
import de.visualdigits.kotlin.util.get
import de.visualdigits.kotlin.util.post
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Base64
import kotlin.random.Random

const val UDP_PORT = 5555 // scraped from elsewhere...
const val UDP_PORT_MUSIC = 5556 // scraped from elsewhere...
const val UDP_PORT_STREAMING = 7777 // scraped from elsewhere...

const val CONNECTION_TIMEOUT = 5000

const val HEADER_X_AUTH_TOKEN = "X-Auth-Token"

/**
 * Base class for specific twinkly devices.
 * Handles login and out.
 */
abstract class Session(
    val ipAddress: String,
    val baseUrl: String
) {

    val log = LoggerFactory.getLogger(javaClass)

    companion object {
        val tokens: MutableMap<String, AuthToken> = mutableMapOf()
    }

    /**
     * Performs the challenge response sequence needed to actually log in to the device.
     */
    fun login() {
        if (!isLoggedIn()) { // avoid additional attempts from other instances if we already know that we cannot talk to the host
            log.debug("#### Logging into device at ip address $ipAddress...")
            val challenge = Base64.getEncoder().encode(Random(System.currentTimeMillis()).nextBytes(32)).decodeToString()
            post<Map<String, Any>>(
                url = "$baseUrl/login",
                body = "{\"challenge\":\"$challenge\"}".toByteArray(),
                headers = mutableMapOf(
                    "Content-Type" to "application/json"
                )
            )?.also { response ->
                val authToken = response["authentication_token"] as String
                val expireInSeconds = (response["authentication_token_expires_in"] as Int)
                val responseVerify = verify((response["challenge-response"] as String), authToken)
                if (responseVerify?.responseCode != ResponseCode.Ok) {
                    log.warn("Could not login to device at ip address '$ipAddress'")
                }
                val tokenExpires = System.currentTimeMillis() + expireInSeconds * 1000 - 5000
                log.debug("#### Token expires '${formatEpoch(tokenExpires)}'")
                tokens[ipAddress] = AuthToken(authToken, tokenExpires, true)
            }?:also {
                tokens[ipAddress] = AuthToken(loggedIn = false)
            }
        }
   }

    /**
     * The current token expires after a device chosen time and has then to be refreshed.
     * Current token is removed and a new login is performed.
     */
    fun refreshTokenIfNeeded() {
        if (System.currentTimeMillis() > (tokens[ipAddress]?.tokenExpires?:0)) {
            log.debug("Refreshing token for device at ip address '$ipAddress'...")
            tokens.remove(ipAddress)
            login()
        }
    }

    /**
     * Validates the current
     */
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

    /**
     * Determines if the session is logged into the device.
     */
    fun isLoggedIn(): Boolean = tokens.containsKey(ipAddress) && tokens[ipAddress]?.loggedIn == true

    /**
     * Logs the session out of the device.
     */
    open fun logout() {
        post<JsonObject>("$baseUrl/logout")
    }

    /**
     * Returns the devices current status.
     */
    fun status(): JsonObject? {
        return get<JsonObject>(
            url = "$baseUrl/status",
        )
    }

    /**
     * Returns infos about the network configuration of the device.
     */
    fun networkStatus(): NetworkStatus? {
        return get<NetworkStatus>(
            url = "$baseUrl/network/status",
        )
    }

    /**
     * Converts the given seconds since epoch to ISO formatted date time string.
     */
    private fun formatEpoch(epoch: Long): String {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(epoch),
                ZoneId.systemDefault()
            )
        )
    }

    /**
     * Posts the given body bytes toi the url and returns the devices response.
     */
    inline fun <reified T> post(
        url: String,
        body: ByteArray = byteArrayOf(),
        headers: MutableMap<String, String> = mutableMapOf(),
        authToken: String? = null
    ): T? {
        log.debug("POST '{}' body='{}' headers={}", url, String(body), headers)
        (authToken?: tokens[ipAddress]?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return runCatching {
            URL(url).post<T>(body, headers)
        }.onFailure {
            log.warn("Could not talk to device at ip address '$ipAddress'")
        }.getOrNull()
    }

    /**
     * Returns the response from the given url.
     */
    inline fun <reified T> get(
        url: String,
        headers: MutableMap<String, String> = mutableMapOf(),
        authToken: String? = null
    ): T? {
        log.debug("GET '{}' headers={}", url, headers)
        (authToken?: tokens[ipAddress]?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return runCatching {
            URL(url).get<T>(headers)
        }.onFailure {
            log.warn("Could not talk to device at ip address '$ipAddress'")
        }.getOrNull()
    }

    inline fun <reified T> delete(
        url: String,
        headers: MutableMap<String, String> = mutableMapOf(),
        authToken: String? = null
    ): T? {
        log.debug("DELETE '{}' headers={}", url, headers)
        (authToken?: tokens[ipAddress]?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return runCatching {
            URL(null, url).delete<T>(headers)
        }.onFailure {
            log.warn("Could not talk to device at ip address '$ipAddress'")
        }.getOrNull()
    }
}
