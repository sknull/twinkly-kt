package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.common.networkstatus.NetworkStatus
import de.visualdigits.kotlin.twinkly.model.device.AuthToken
import de.visualdigits.kotlin.twinkly.model.device.xled.request.CurrentMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.request.NewMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.device.xled.response.FirmwareVersionResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.PlayList
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Version
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.CurrentLedEffectResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedConfigResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedEffectsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedLayout
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.LedMode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.Mode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.CurrentMovieResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.LedMovieConfigResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.Movie
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.Movies
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.NewMovieResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicDriverSetResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicDriversResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicEffectResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.LedMusicStatsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicDriversSets
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicEffectsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicEnabledResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MusicConfig
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import de.visualdigits.kotlin.udp.UdpClient
import de.visualdigits.kotlin.util.TimeUtil
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
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Base class for specific twinkly devices.
 * Handles login and out.
 */
open class XLedDevice(
    val ipAddress: String,
    val baseUrl: String = "http://$ipAddress/xled/v1",
    var width: Int = 0,
    var height: Int = 0,
    val transformation: ((XledFrame) -> XledFrame)? = null
) {

    val deviceInfo: DeviceInfo?
    val firmwareVersion: Version
    val deviceGeneration: Int
    val ledMovieConfig: LedMovieConfigResponse?
    val bytesPerLed: Int
    val ledLayout: LedLayout?

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
                firmwareVersion = getFirmwareVersionResponse()?.versionParts ?: Version.Companion.UNKNOWN
                deviceGeneration = determineDeviceGeneration()
                ledLayout = getLedLayoutResponse()
                bytesPerLed = deviceInfo?.bytesPerLed?:3
                ledMovieConfig = getLedMovieConfigResponse()
            } else {
                deviceInfo = null
                firmwareVersion = Version.Companion.UNKNOWN
                deviceGeneration = 0
                ledLayout = null
                bytesPerLed = 0
                ledMovieConfig = null
            }
        } else {
            deviceInfo = null
            firmwareVersion = Version.Companion.UNKNOWN
            deviceGeneration = 0
            ledLayout = null
            bytesPerLed = 0
            ledMovieConfig = null
        }
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
                val authToken = response["authentication_token"] as String
                val expireInSeconds = (response["authentication_token_expires_in"] as Int)
                val responseVerify = verify((response["challenge-response"] as String), authToken)
                if (responseVerify?.responseCode != ResponseCode.Ok) {
                    log.warn("Could not login to device at ip address '$ipAddress'")
                }
                val tokenExpires = System.currentTimeMillis() + expireInSeconds * 1000 - 5000
                log.debug("#### Token expires '${formatEpoch(tokenExpires)}'")
                tokens[ipAddress] = AuthToken(authToken, tokenExpires, true)
            } ?: also {
                tokens[ipAddress] = AuthToken(loggedIn = false)
            }
        }
    }

    open fun getLedMovieConfigResponse(): LedMovieConfigResponse? {
        return get<LedMovieConfigResponse>(
            url = "$baseUrl/led/movie/config",
            clazz = LedMovieConfigResponse::class.java
        )
    }

    /**
     * The current token expires after a device chosen time and has then to be refreshed.
     * Current token is removed and a new login is performed.
     */
    fun refreshTokenIfNeeded() {
        if (System.currentTimeMillis() > (tokens[ipAddress]?.tokenExpires ?: 0)) {
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
    open fun isLoggedIn(): Boolean = tokens.containsKey(ipAddress) && tokens[ipAddress]?.loggedIn == true

    /**
     * Logs the session out of the device.
     */
    open fun logout() {
        post<JsonObject>("$baseUrl/logout", clazz = JsonObject::class.java)
    }

    open fun powerOn() {
        refreshTokenIfNeeded()
        // try modes until it works...
        listOf(LedMode.playlist, LedMode.movie, LedMode.effect)
            .find { mode -> setLedMode(mode)?.responseCode == ResponseCode.Ok }
    }

    open fun powerOff() {
        refreshTokenIfNeeded()
        setLedMode(LedMode.off)
    }

    open fun getMode(): Mode? {
        refreshTokenIfNeeded()
        return get<Mode>(
            url = "$baseUrl/led/mode",
            clazz = Mode::class.java
        )
    }

    open fun getDeviceMode(): LedMode? {
        return getMode()?.ledMode
    }

    open fun setLedMode(ledMode: LedMode): JsonObject? {
        refreshTokenIfNeeded()
        val body = "{\"mode\":\"${ledMode.name}\"}"
        log.debug("Setting mode for device '$ipAddress' to ${ledMode.name}...")
        return post<Mode>(
            url = "http://$ipAddress/xled/v1/led/mode",
            body = body.toByteArray(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            ),
            clazz = Mode::class.java
        )
    }

    open fun getLedLayoutResponse(): LedLayout? {
        return get<LedLayout>(
            url = "$baseUrl/led/layout/full",
            clazz = LedLayout::class.java)
    }

    open fun ledReset() {
        refreshTokenIfNeeded()
        get<String>(
            url = "$baseUrl/led/reset",
            clazz = String::class.java)
    }

    open fun getMusicEffects(): MusicEffectsResponse? {
        return get<MusicEffectsResponse>(
            url = "$baseUrl/music/effects",
            clazz = MusicEffectsResponse::class.java
        )
    }

    open fun getCurrentMusicEffect(): CurrentMusicEffectResponse? {
        return get<CurrentMusicEffectResponse>(
            url = "$baseUrl/music/effects/current",
            clazz = CurrentMusicEffectResponse::class.java
        )
    }

    open fun setCurrentMusicEffect(effectId: String): JsonObject? {
        return post<JsonObject>(
            url = "/$baseUrl/music/effects/current",
            body = ("{\n" +
                    "  \"effect_idx\": 7,\n" +
                    "  \"effectset_idx\": 6,\n" +
                    "  \"effectsuperset_idx\": 1,\n" +
                    "  \"mood_index\": 0\n" +
                    "}").toByteArray(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            ),
            clazz = JsonObject::class.java
        )
    }

    open fun getMusicConfig(): MusicConfig? {
        val response = get<MusicConfig>(
            url = "$baseUrl/music/config",
            clazz = MusicConfig::class.java
        )
        return response
    }

    open fun getLedMusicStats(): LedMusicStatsResponse? {
        refreshTokenIfNeeded()
        return get<LedMusicStatsResponse>(
            url = "$baseUrl/music/stats",
            clazz = LedMusicStatsResponse::class.java
        )
    }

    open fun getMusicEnabled(): MusicEnabledResponse? {
        refreshTokenIfNeeded()
        return get<MusicEnabledResponse>(
            url = "$baseUrl/music/enabled",
            clazz = MusicEnabledResponse::class.java
        )
    }

    open fun setMusicEnabled(enabled: Boolean): JsonObject? {
        refreshTokenIfNeeded()
        return post<JsonObject>(
            url = "$baseUrl/music/enabled",
            body = "{\"enabled\":${if (enabled) 1 else 0}}".toByteArray(),
            clazz = JsonObject::class.java
        )
    }

    open fun getMusicDriversCurrent(): CurrentMusicDriversResponse? {
        refreshTokenIfNeeded()
        return get<CurrentMusicDriversResponse>(
            url = "$baseUrl/music/drivers/current",
            clazz = CurrentMusicDriversResponse::class.java
        )
    }

    open fun getMusicDriversSets(): MusicDriversSets? {
        refreshTokenIfNeeded()
        return get<MusicDriversSets>(
            url = "$baseUrl/music/drivers/sets",
            clazz = MusicDriversSets::class.java
        )
    }

    open fun getCurrentMusicDriversSet(): CurrentMusicDriverSetResponse? {
        refreshTokenIfNeeded()
        return get<CurrentMusicDriverSetResponse>(
            url = "$baseUrl/music/drivers/sets/current",
            clazz = CurrentMusicDriverSetResponse::class.java
        )
    }

    open fun getBrightness(): Brightness? {
        refreshTokenIfNeeded()
        return get<Brightness>(
            url = "$baseUrl/led/out/brightness",
            clazz = Brightness::class.java
        )
    }

    open fun setBrightness(brightness: Float) {
        refreshTokenIfNeeded()
        post<JsonObject>(
            url = "$baseUrl/led/out/brightness",
            body = Brightness(value = (100 * brightness).roundToInt()).writeValueAssBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            clazz = JsonObject::class.java
        )
    }

    open fun getSaturation(): Saturation? {
        refreshTokenIfNeeded()
        return get<Saturation>(
            url = "$baseUrl/led/out/saturation",
            clazz = Saturation::class.java
        )
    }

    open fun setSaturation(saturation: Float) {
        refreshTokenIfNeeded()
        post<JsonObject>(
            url = "$baseUrl/led/out/saturation",
            body = Saturation(value = (100 * saturation).roundToInt()).writeValueAssBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            ),
            clazz = JsonObject::class.java
        )
    }

    open fun getColor(): Color<*> {
        refreshTokenIfNeeded()
        val response = get<Map<*,*>>(
            url = "$baseUrl/led/color",
            clazz = Map::class.java
        )
        return if (response != null) {
            if (response["red"] as Int > 0 || response["green"] as Int > 0 || response["blue"] as Int > 0 || response["white"] as Int > 0) {
                if (response["white"] as Int > 0) {
                    RGBWColor(
                        red = response["red"] as Int,
                        green = response["green"] as Int,
                        blue = response["blue"] as Int,
                        white = response["white"] as Int,
                    )
                } else {
                    RGBColor(
                        red = response["red"] as Int,
                        green = response["green"] as Int,
                        blue = response["blue"] as Int
                    )
                }
            } else if (response["hue"] as Int > 0 || response["saturation"] as Int > 0 || response["value"] as Int > 0) {
                HSVColor(
                    h = response["hue"] as Int,
                    s = ((response["saturation"] as Int) / 255.0 * 100.0).toInt(),
                    v = ((response["value"] as Int) / 255.0 * 100.0).toInt()
                )
            } else {
                RGBColor()
            }
        } else {
            RGBColor()
        }
    }

    open fun setColor(color: Color<*>) {
        refreshTokenIfNeeded()
        val body = when (color) {
            is RGBColor -> "{\"red\":${color.red},\"green\":${color.green},\"blue\":${color.blue}}"
            is RGBWColor -> "{\"red\":${color.red},\"green\":${color.green},\"blue\":${color.blue},\"white\":${color.white}}"
            is HSVColor -> "{\"hue\":${color.h},\"saturation\":${(color.s / 100.0 * 255.0).toInt()},\"value\":${{(color.v / 100.0 * 255.0).toInt()}}"
            else -> {
                log.warn("Unsupported color model '${color::class.simpleName}' - converting to rgb")
                val rgbColor = color.toRgbColor()
                "{\"red\":${rgbColor.red},\"green\":${rgbColor.green},\"blue\":${rgbColor.blue}}"
            }
        }
        post<JsonObject>(
            url = "$baseUrl/led/color",
            body = body.toByteArray(),
            headers = mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            clazz = JsonObject::class.java
        )
    }

    open fun getLedConfig(): LedConfigResponse? {
        refreshTokenIfNeeded()
        return get<LedConfigResponse>(
            url = "$baseUrl/led/config",
            clazz = LedConfigResponse::class.java
        )
    }

    open fun getLedEffects(): LedEffectsResponse? {
        return get<LedEffectsResponse>(
            url = "$baseUrl/led/effects",
            clazz = LedEffectsResponse::class.java
        )
    }

    open fun getCurrentLedEffect(): CurrentLedEffectResponse? {
        return get<CurrentLedEffectResponse>(
            url = "$baseUrl/led/effects/current",
            clazz = CurrentLedEffectResponse::class.java
        )
    }

    open fun setCurrentLedEffect(effectId: String): JsonObject? {
        return post<JsonObject>(
            url = "$baseUrl/led/effects/current",
            body = "{\"effect_id\": \"$effectId\"}".toByteArray(),
            headers = mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            clazz = JsonObject::class.java
        )
    }

    open fun getMovies(): Movies? {
        return get<Movies>(
            url = "$baseUrl/movies",
            clazz = Movies::class.java
        )
    }

    open fun deleteMovies(): JsonObject? {
        return delete<JsonObject>(
            url = "$baseUrl/movies",
            clazz = JsonObject::class.java
        )
    }

    open fun getCurrentMovie(): CurrentMovieResponse? {
        return get<CurrentMovieResponse>(
            url = "$baseUrl/movies/current",
            clazz = CurrentMovieResponse::class.java
        )
    }

    open fun setCurrentMovie(currentMovieRequest: CurrentMovieRequest): JsonObject? {
        return post<JsonObject>(
            url = "$baseUrl/movies/current",
            body = currentMovieRequest.writeValueAssBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            clazz = JsonObject::class.java
        )
    }

    open fun getPlaylist(): PlayList? {
        return get<PlayList>(
            url = "$baseUrl/playlist",
            clazz = PlayList::class.java
        )
    }

    open fun getPlaylistCurrent(): String? {
        return get<String>(
            url = "$baseUrl/playlist/current",
            clazz = String::class.java
        )
    }

    open fun showFrame(
        name: String,
        frame: XledFrame
    ) {
        showSequence(name, XledSequence(frames = mutableListOf(frame)), 1)
    }

    /**
     * Experimental code which tries to upload a new movie and plays it in device.
     * Seems to overwrite the current sequence which is active in the device.
     */
    open fun showSequence(
        name: String,
        sequence: XledSequence,
        fps: Int
    ) {
        setColor(RGBColor(0, 0, 0))
        setLedMode(LedMode.color)
        deleteMovies()

        val numberOfFrames = sequence.size

        val newMovie = uploadNewMovie(
            NewMovieRequest(
                name = name.substring(0, min(name.length, 32)),
                descriptorType = "rgbw_raw",
                ledsPerFrame = bytesPerLed,
                framesNumber = numberOfFrames,
                fps = fps
            )
        )
        uploadNewMovieToListOfMovies(sequence.toByteArray(bytesPerLed))
        setLedMovieConfig(
            LedMovieConfigResponse(
                frameDelay = 1000 / fps,
                ledsNumber = bytesPerLed,
                framesNumber = numberOfFrames,
            )
        )
        setCurrentMovie(CurrentMovieRequest(id = newMovie?.id))

        setLedMode(LedMode.movie)
    }

    open fun showRealTimeFrame(frame: XledFrame) {
        val transformed = transformation?.let {
            transformation!!(frame)
        }?:frame
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

    private fun createDatagramV1(value: List<Byte>): ByteArray {
        return byteArrayOf(0x01) +
                Base64.getDecoder().decode(tokens[ipAddress]?.authToken ?: "") +
                byteArrayOf(value.size.toByte()) +
                value.toByteArray()
    }

    private fun createDatagramV2(value: List<Byte>): ByteArray {
        return byteArrayOf(0x02) +
                Base64.getDecoder().decode(tokens[ipAddress]?.authToken ?: "") +
                byteArrayOf(0x00) +
                value.toByteArray()
    }

    private fun createDatagramV3(index: Int, value: List<Byte>): ByteArray {
        return byteArrayOf(0x03) +
                Base64.getDecoder().decode(tokens[ipAddress]?.authToken ?: "") +
                byteArrayOf(0x00, 0x00) +
                byteArrayOf(index.toByte()) +
                value.toByteArray()
    }

    fun showRealTimeSequence(
        frameSequence: XledSequence,
        loop: Int = 1
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

    open fun setLedMovieConfig(movieConfig: LedMovieConfigResponse): JsonObject? {
        return post<JsonObject>(
            url = "$baseUrl/led/movie/config",
            body = movieConfig.writeValueAssBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            clazz = JsonObject::class.java
        )
    }

    open fun uploadNewMovie(newMovie: NewMovieRequest): NewMovieResponse? {
        return post<NewMovieResponse>(
            url = "$baseUrl/movies/new",
            body = newMovie.writeValueAssBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            clazz = NewMovieResponse::class.java
        )
    }

    open fun uploadNewMovieToListOfMovies(frame: XledFrame): Movie? {
        val bytes = frame.toByteArray(bytesPerLed)
        return uploadNewMovieToListOfMovies(bytes)
    }

    open fun uploadNewMovieToListOfMovies(bytes: ByteArray): Movie? {
        return post<Movie>(
            url = "$baseUrl/led/movie/full",
            body = bytes,
            headers = mutableMapOf(
                "Content-Type" to "application/octet-stream",
                "Accept" to "application/json"
            ),
            clazz = Movie::class.java
        )
    }

    open fun getTimer(): Timer? {
        return get<Timer>(
            url = "$baseUrl/timer",
            clazz = Timer::class.java
        )
    }

    open fun setTimer(
        timeOn: OffsetDateTime,
        timeOff: OffsetDateTime
    ): Timer? {
        return setTimer(
            timeOnHour = timeOn.hour,
            timeOnMinute = timeOn.minute,
            timeOffHour = timeOff.hour,
            timeOffMinute = timeOff.minute,
        )
    }

    open fun setTimer(
        timeOnHour: Int,
        timeOnMinute: Int,
        timeOffHour: Int,
        timeOffMinute: Int
    ): Timer? {
        val timer = Timer(
            timeNow = TimeUtil.utcSecondsAfterMidnight(),
            timeOn = TimeUtil.utcSecondsAfterMidnight(timeOnHour, timeOnMinute),
            timeOff = TimeUtil.utcSecondsAfterMidnight(timeOffHour, timeOffMinute)
        )
        return setTimer(timer)
    }

    open fun setTimer(timer: Timer): Timer? {
        val result = post<JsonObject>(
            url = "$baseUrl/timer",
            body = timer.writeValueAssBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            clazz = JsonObject::class.java
        )
        return if (result?.responseCode == ResponseCode.Ok) {
            getTimer()
        } else {
            log.warn("Could not set timer")
            null
        }
    }

    open fun getDeviceInfoResponse(): DeviceInfo? {
        // do not rename to avoid name clash with above attribute
        return get<DeviceInfo>(
            url = "$baseUrl/gestalt",
            clazz = DeviceInfo::class.java
        )
    }

    open fun getFirmwareVersionResponse(): FirmwareVersionResponse? {
        // do not rename to avoid name clash with above attribute
        return get<FirmwareVersionResponse>(
            url = "$baseUrl/fw/version",
            clazz = FirmwareVersionResponse::class.java
        )
    }

    open fun determineDeviceGeneration(): Int {
        return if (deviceInfo?.fwFamily == "D" && firmwareVersion <= Version("2.3.8")) {
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
        (authToken ?: tokens[ipAddress]?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return URL(url).post<T>(body, headers, clazz)
    }

    /**
     * Returns the response from the given url.
     */
    fun <T : Any> get(
        url: String,
        headers: MutableMap<String, String> = mutableMapOf<String, String>(),
        clazz: Class<T>
    ): T? {
        tokens[ipAddress]?.authToken?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return URL(url).get<T>(headers, clazz)
    }

    fun <T : Any> delete(
        url: String,
        headers: MutableMap<String, String> = mutableMapOf<String, String>(),
        clazz: Class<T>
    ): T? {
        tokens[ipAddress]?.authToken?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return URL(null, url).delete<T>(headers, clazz)
    }
}
