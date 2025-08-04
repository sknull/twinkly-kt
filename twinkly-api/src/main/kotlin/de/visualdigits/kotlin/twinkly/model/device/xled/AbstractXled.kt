package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.common.networkstatus.NetworkStatus
import de.visualdigits.kotlin.twinkly.model.device.AuthToken
import de.visualdigits.kotlin.twinkly.model.device.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.device.xled.response.FirmwareVersionResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Version
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedLayout
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.LedMovieConfigResponse
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import de.visualdigits.kotlin.udp.UdpClient
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

abstract class AbstractXled(
    val ipAddress: String,
    val baseUrl: String,
    val transformation: ((XledFrame) -> XledFrame)? = null
) {

    val log = LoggerFactory.getLogger(javaClass)

    val deviceInfo: DeviceInfo?
    val firmwareVersion: Version
    val deviceGeneration: Int
    val ledMovieConfig: LedMovieConfigResponse?
    val bytesPerLed: Int
    val ledLayout: LedLayout?

    private var authToken: AuthToken? = null

    companion object {
        const val UDP_PORT_STREAMING = 7777 // scraped from elsewhere...

        const val HEADER_X_AUTH_TOKEN = "X-Auth-Token"
    }

    init {
        if (ipAddress.isNotEmpty()) {
            // ensure we are logged in up to here to avoid unneeded requests
            if (authToken == null) login()
            if (authToken?.loggedIn == true) {
                deviceInfo = getDeviceInfoResponse()
                firmwareVersion = getFirmwareVersionResponse()?.versionParts ?: Version.Companion.UNKNOWN
                deviceGeneration = determineDeviceGeneration()
                ledLayout = getLedLayoutResponse()
                bytesPerLed = deviceInfo?.bytesPerLed?:3
                ledMovieConfig = getLedMovieConfigResponse()
            } else {
                deviceInfo = null
                firmwareVersion = Version.UNKNOWN
                deviceGeneration = 0
                ledLayout = null
                bytesPerLed = 0
                ledMovieConfig = null
            }
        } else {
            deviceInfo = null
            firmwareVersion = Version.UNKNOWN
            deviceGeneration = 0
            ledLayout = null
            bytesPerLed = 0
            ledMovieConfig = null
        }
    }

    private fun getDeviceInfoResponse(): DeviceInfo? {
        // do not rename to avoid name clash with above attribute
        return get<DeviceInfo>(
            url = "$baseUrl/gestalt",
            clazz = DeviceInfo::class.java
        )
    }

    private fun getFirmwareVersionResponse(): FirmwareVersionResponse? {
        // do not rename to avoid name clash with above attribute
        return get<FirmwareVersionResponse>(
            url = "$baseUrl/fw/version",
            clazz = FirmwareVersionResponse::class.java
        )
    }

    private fun determineDeviceGeneration(): Int {
        return if (deviceInfo?.fwFamily == "D" && firmwareVersion <= Version("2.3.8")) {
            1
        } else if (firmwareVersion <= Version("2.4.6")) {
            2
        } else {
            3
        }
    }

    open fun getLedMovieConfigResponse(): LedMovieConfigResponse? {
        return get<LedMovieConfigResponse>(
            url = "$baseUrl/led/movie/config",
            clazz = LedMovieConfigResponse::class.java
        )
    }

    open fun getLedLayoutResponse(): LedLayout? {
        return get<LedLayout>(
            url = "$baseUrl/led/layout/full",
            clazz = LedLayout::class.java)
    }

    /**
     * Performs the challenge response sequence needed to actually log in to the device.
     */
    fun login() {
        if (!isLoggedIn()) { // avoid additional attempts from other instances if we already know that we cannot talk to the host
            log.debug("#### Logging into device at ip address $ipAddress...")
            val challenge =
                Base64.getEncoder().encode(Random(System.currentTimeMillis()).nextBytes(32)).decodeToString()
            post<Map<*, *>>(
                url = "$baseUrl/login",
                body = "{\"challenge\":\"$challenge\"}".toByteArray(),
                headers = mutableMapOf(
                    "Content-Type" to "application/json",
                    "Accept" to "application/json"
                ),
                clazz = Map::class.java
            )?.also { response ->
                val token = response["authentication_token"] as String
                val expireInSeconds = (response["authentication_token_expires_in"] as Int)
                val responseVerify = verify((response["challenge-response"] as String), token)
                if (responseVerify?.responseCode != ResponseCode.Ok) {
                    log.warn("Could not login to device at ip address '$ipAddress'")
                }
                val tokenExpires = System.currentTimeMillis() + expireInSeconds * 1000 - 5000
                log.debug("#### Token expires '${formatEpoch(tokenExpires)}'")
                authToken = AuthToken(token, tokenExpires, true)
            } ?: also {
                authToken = AuthToken(loggedIn = false)
            }
        }
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
     * Determines if the session is logged into the device.
     */
    open fun isLoggedIn(): Boolean = authToken?.loggedIn == true

    /**
     * Logs the session out of the device.
     */
    open fun logout() {
        post<JsonObject>("$baseUrl/logout", clazz = JsonObject::class.java)
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
     * Returns the devices current status.
     */
    fun getStatus(): JsonObject? {
        return get<JsonObject>(
            url = "$baseUrl/status",
            clazz = JsonObject::class.java
        )
    }


    /**
     * Returns infos about the network configuration of the device.
     */
    fun getNetworkStatus(): NetworkStatus? {
        return get<NetworkStatus>(
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
     * The current token expires after a device chosen time and has then to be refreshed.
     * Current token is removed and a new login is performed.
     */
    fun refreshTokenIfNeeded() {
        if (System.currentTimeMillis() > (authToken?.tokenExpires ?: 0)) {
            log.debug("Refreshing token for device at ip address '$ipAddress'...")
            authToken = null
            login()
        }
    }

    open fun showRealTimeFrame(frame: XledFrame) {
        val transformed = transformation?.let { t -> t(frame) }?:frame
        UdpClient(ipAddress, UDP_PORT_STREAMING).use { udpClient ->
            transformed.toByteArray(bytesPerLed)
                .toList()
                .chunked(900)
                .mapIndexed { index, value ->
                    val datagram = when (deviceGeneration) {
                        1 -> createDatagramV1(value)
                        2 -> createDatagramV2(value)
                        3 -> createDatagramV3(index, value)
                        else -> null
                    }
                    datagram?.also { d -> udpClient.send(d) }
                }
        }
    }

    open fun showRealTimeSequence(
        frameSequence: XledSequence,
        loop: Int
    ) {
        val frames = frameSequence
            .filter { it is XledFrame }
            .map { it as XledFrame }

        var loopCount = loop
        while (loopCount == -1 || loopCount > 0) {
            frames.forEach { frame ->
                showRealTimeFrame(frame)
                Thread.sleep(frameSequence.frameDelay)
            }
            if (loopCount != -1) loopCount--
        }
    }

    private fun createDatagramV1(value: List<Byte>): ByteArray {
        return byteArrayOf(0x01) +
                Base64.getDecoder().decode(authToken?.authToken ?: "") +
                byteArrayOf(value.size.toByte()) +
                value.toByteArray()
    }

    private fun createDatagramV2(value: List<Byte>): ByteArray {
        return byteArrayOf(0x02) +
                Base64.getDecoder().decode(authToken?.authToken ?: "") +
                byteArrayOf(0x00) +
                value.toByteArray()
    }

    private fun createDatagramV3(index: Int, value: List<Byte>): ByteArray {
        return byteArrayOf(0x03) +
                Base64.getDecoder().decode(authToken?.authToken ?: "") +
                byteArrayOf(0x00, 0x00) +
                byteArrayOf(index.toByte()) +
                value.toByteArray()
    }

    /**
     * Posts the given body bytes toi the url and returns the devices response.
     */
    fun <T : Any> post(
        url: String,
        body: ByteArray = byteArrayOf(),
        headers: MutableMap<String, String> = mutableMapOf(),
        authToken: String? = null,
        clazz: Class<T>
    ): T? {
        (authToken ?: this.authToken?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }

        return URL(url).post<T>(body, headers, clazz)
    }

    /**
     * Returns the response from the given url.
     */
    fun <T : Any> get(
        url: String,
        headers: MutableMap<String, String> = mutableMapOf(),
        clazz: Class<T>
    ): T? {
        authToken?.authToken?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return URL(url).get<T>(headers, clazz)
    }

    fun <T : Any> delete(
        url: String,
        headers: MutableMap<String, String> = mutableMapOf(),
        clazz: Class<T>
    ): T? {
        authToken?.authToken?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return URL(null, url).delete<T>(headers, clazz)
    }
}
