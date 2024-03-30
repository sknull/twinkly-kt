package de.visualdigits.kotlin.twinkly.model.device

import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.common.networkstatus.NetworkStatus
import de.visualdigits.kotlin.twinkly.model.device.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import de.visualdigits.kotlin.util.UrlClient
import org.slf4j.LoggerFactory
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
    host: String,
    val baseUrl: String
): UrlClient(host) {

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
                    log.warn("Could not login to device $host")
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

    fun isLoggedIn(): Boolean = tokens.containsKey(host) && tokens[host]?.loggedIn == true

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

    protected fun formatEpoch(epoch: Long): String {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(epoch),
                ZoneId.systemDefault()
            )
        )
    }
}
