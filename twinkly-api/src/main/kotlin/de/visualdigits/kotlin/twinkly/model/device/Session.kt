package de.visualdigits.kotlin.twinkly.model.device

import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.common.networkstatus.NetworkStatus
import de.visualdigits.kotlin.twinkly.model.device.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.device.xled.response.FirmwareVersionResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Version
import de.visualdigits.kotlin.util.delete
import de.visualdigits.kotlin.util.get
import de.visualdigits.kotlin.util.post
import org.slf4j.LoggerFactory
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.net.URL
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Base64
import kotlin.random.Random


/**
 * Base class for specific twinkly devices.
 * Handles login and out.
 */
abstract class Session(
    val ipAddress: String,
    val baseUrl: String
) {

    val deviceInfo: DeviceInfo?
    val firmwareVersion: Version
    val deviceGeneration: Int

    val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val UDP_PORT_DISCOVER = 5555 // scraped from elsewhere...
        const val UDP_PORT_MUSIC = 5556 // scraped from elsewhere...
        const val UDP_PORT_STREAMING = 7777 // scraped from elsewhere...

        const val CONNECTION_TIMEOUT = 5000

        const val HEADER_X_AUTH_TOKEN = "X-Auth-Token"

        val tokens: MutableMap<String, AuthToken> = mutableMapOf()

        fun discoverTwinklyDevices(timeoutMillis: Int = 2000): List<String> {
            val devices = mutableListOf<String>()
            val socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = timeoutMillis

            // Twinkly devices respond to this "discover" message
            val data = ByteArray(1) + "discover".toByteArray()
            val packet = DatagramPacket(
                data,
                data.size,
                InetAddress.getByName("255.255.255.255"),
                5555
            )

            // Send broadcast
            socket.send(packet)

            val buffer = ByteArray(1024)
            val receivePacket = DatagramPacket(buffer, buffer.size)
            val stopTime = System.currentTimeMillis() + timeoutMillis
            while (System.currentTimeMillis() < stopTime) {
                try {
                    socket.receive(receivePacket)
                    String(receivePacket.data, 0, receivePacket.length)
                    devices.add(receivePacket.address.hostAddress)
                } catch (_: SocketTimeoutException) {
                    break
                }
            }
            socket.close()

            return devices.distinct()
        }
    }

    init {
        if (ipAddress.isNotEmpty()) {
            // ensure we are logged in up to here to avoid unneeded requests
            if (!tokens.containsKey(ipAddress)) login()
            if (tokens[ipAddress]?.loggedIn == true) {
                deviceInfo = getDeviceInfoResponse()
                firmwareVersion = getFirmwareVersionResponse()?.versionParts ?: Version.UNKNOWN
                deviceGeneration = determineDeviceGeneration()
            } else {
                deviceInfo = null
                firmwareVersion = Version.UNKNOWN
                deviceGeneration = 0
            }
        } else {
            deviceInfo = null
            firmwareVersion = Version.UNKNOWN
            deviceGeneration = 0
        }
    }

    /**
     * Performs the challenge response sequence needed to actually log in to the device.
     */
    fun login() {
        if (!isLoggedIn()) { // avoid additional attempts from other instances if we already know that we cannot talk to the host
            log.debug("#### Logging into device at ip address $ipAddress...")
            val challenge = Base64.getEncoder().encode(Random(System.currentTimeMillis()).nextBytes(32)).decodeToString()
            post<Map<*, *>>(
                url = "$baseUrl/login",
                body = "{\"challenge\":\"$challenge\"}".toByteArray(),
                headers = mutableMapOf(
                    "Content-Type" to "application/json",
                    "Accept" to "application/json"
                ),
                clazz = Map::class.java
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
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            authToken = authToken,
            clazz = JsonObject::class.java
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
        post<JsonObject>("$baseUrl/logout", clazz = JsonObject::class.java)
    }

    fun getDeviceInfoResponse(): DeviceInfo? {
        // do not rename to avoid name clash with above attribute
        return get<DeviceInfo>(
            url = "$baseUrl/gestalt",
            clazz = DeviceInfo::class.java
        )
    }

    fun getFirmwareVersionResponse(): FirmwareVersionResponse? {
        // do not rename to avoid name clash with above attribute
        return get<FirmwareVersionResponse>(
            url = "$baseUrl/fw/version",
            clazz = FirmwareVersionResponse::class.java
        )
    }

    fun determineDeviceGeneration(): Int {
        return if (deviceInfo?.fwFamily == "D" &&  firmwareVersion <= Version("2.3.8")) {
            1
        } else if (firmwareVersion <= Version("2.4.6")) {
            2
        } else {
            3
        }
    }

    /**
     * Returns the devices current status.
     */
    fun getStatus(): JsonObject? {
        return get<JsonObject>(
            url = "$baseUrl/status",
            clazz = JsonObject::class.java
        )
    }

    /**
     * Returns the devices group infos.
     */
    fun getGroup(): String? {
        return get<String>(
            url = "$baseUrl/group/status",
            clazz = String::class.java
        )
    }

    /**
     * Returns infos about the network configuration of the device.
     */
    fun getNetworkStatus(): NetworkStatus? {
        return get(
            url = "$baseUrl/network/status",
            clazz = NetworkStatus::class.java
        )
    }

    fun getEndpoint(uri: String): String? {
        val response = get<String>(
            url = "$baseUrl$uri",
            clazz = String::class.java
        )
        return response
    }

    fun getEndpointRaw(uri: String): String? {
        val response = get<String>(
            url = "$uri",
            clazz = String::class.java
        )
        return response
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
    fun <T : Any> post(
        url: String,
        body: ByteArray = byteArrayOf(),
        headers: MutableMap<String, String> = mutableMapOf<String, String>(),
        authToken: String? = null,
        clazz: Class<T>
    ): T? {
        (authToken?: tokens[ipAddress]?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return URL(url).post<T>(body, headers, clazz)
    }

    /**
     * Returns the response from the given url.
     */
    fun <T : Any> get(
        url: String,
        headers: MutableMap<String, String> = mutableMapOf<String, String>(),
        authToken: String? = null,
        clazz: Class<T>
    ): T? {
        (authToken?: tokens[ipAddress]?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return URL(url).get<T>(headers, clazz)
    }

    fun <T : Any> delete(
        url: String,
        headers: MutableMap<String, String> = mutableMapOf<String, String>(),
        authToken: String? = null,
        clazz: Class<T>
    ): T? {
        (authToken?: tokens[ipAddress]?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return URL(null, url).delete<T>(headers, clazz)
    }
}
