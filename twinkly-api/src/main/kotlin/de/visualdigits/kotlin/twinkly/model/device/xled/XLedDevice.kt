package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.Session
import de.visualdigits.kotlin.twinkly.model.device.UDP_PORT_STREAMING
import de.visualdigits.kotlin.twinkly.model.device.xled.request.CurrentMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.request.NewMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.CurrentMovieResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.CurrentMusicDriverSet
import de.visualdigits.kotlin.twinkly.model.device.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Effects
import de.visualdigits.kotlin.twinkly.model.device.xled.response.EffectsCurrent
import de.visualdigits.kotlin.twinkly.model.device.xled.response.FirmwareVersionResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.LedConfig
import de.visualdigits.kotlin.twinkly.model.device.xled.response.MovieConfig
import de.visualdigits.kotlin.twinkly.model.device.xled.response.MusicDriversCurrent
import de.visualdigits.kotlin.twinkly.model.device.xled.response.MusicStats
import de.visualdigits.kotlin.twinkly.model.device.xled.response.NewMovieResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.PlayList
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Version
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ledlayout.LedLayout
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.Mode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movies.Movie
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movies.Movies
import de.visualdigits.kotlin.twinkly.model.device.xled.response.musicdriverssets.MusicDriversSets
import de.visualdigits.kotlin.twinkly.model.device.xled.response.musicenabled.MusicEnabled
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import de.visualdigits.kotlin.udp.UdpClient
import de.visualdigits.kotlin.util.TimeUtil
import de.visualdigits.kotlin.util.compareTo
import java.time.OffsetDateTime
import java.util.Base64
import kotlin.math.min
import kotlin.math.roundToInt

private const val CONTENT_TYPE = "Content-Type"
private const val APPLICATION_JSON = "application/json"

/**
 * Specific session for a twinkly device having leds.
 */
open class XLedDevice(
    ipAddress: String = "",
    override var width: Int = 0,
    override var height: Int = 0,
    override val transformation:  ((XledFrame) -> XledFrame)? = null
): XLed, Session(
    ipAddress,
    "http://$ipAddress/xled/v1",
) {

    val deviceInfo: DeviceInfo?
    val firmwareVersion: Version
    val deviceGeneration: Int
    val ledLayout: LedLayout?

    override val bytesPerLed: Int

    init {
        if (ipAddress.isNotEmpty()) {
            // ensure we are logged in up to here to avoid unneeded requests
            if (!tokens.containsKey(ipAddress)) login()
            if (tokens[ipAddress]?.loggedIn == true) {
                deviceInfo = getDeviceInfoResponse()
                firmwareVersion = getFirmwareVersionResponse()?.versionParts ?: Version.UNKNOWN
                deviceGeneration = determineDeviceGeneration()
                ledLayout = getLedLayoutResponse()
                bytesPerLed = deviceInfo?.bytesPerLed?:3
            } else {
                deviceInfo = null
                firmwareVersion = Version.UNKNOWN
                deviceGeneration = 0
                ledLayout = null
                bytesPerLed = 0
            }
        } else {
            deviceInfo = null
            firmwareVersion = Version.UNKNOWN
            deviceGeneration = 0
            ledLayout = null
            bytesPerLed = 0
        }
    }

    override fun powerOn() {
        refreshTokenIfNeeded()
        // try modes until it works...
        listOf(DeviceMode.playlist, DeviceMode.movie, DeviceMode.effect)
            .find { mode -> setMode(mode)?.responseCode == ResponseCode.Ok }
    }

    override fun powerOff() {
        refreshTokenIfNeeded()
        setMode(DeviceMode.off)
    }

    override fun getMode(): Mode? {
        refreshTokenIfNeeded()
        return get<Mode>(
            url = "$baseUrl/led/mode",
        )
    }

    override fun getDeviceMode(): DeviceMode? {
        return getMode()?.deviceMode
    }

    override fun setMode(mode: DeviceMode): JsonObject? {
        refreshTokenIfNeeded()
        val body = "{\"mode\":\"${mode.name}\"}"
        log.debug("Setting mode for device '$ipAddress' to ${mode.name}...")
        return post<Mode>(
            url = "$baseUrl/led/mode",
            body = body.toByteArray(),
            headers = mutableMapOf(
                CONTENT_TYPE to APPLICATION_JSON
            )
        )
    }

    override fun getDeviceInfoResponse(): DeviceInfo? {
        return get<DeviceInfo>("$baseUrl/gestalt")
    }

    override fun getFirmwareVersionResponse(): FirmwareVersionResponse? {
        return get<FirmwareVersionResponse>("$baseUrl/fw/version")
    }

    override fun determineDeviceGeneration(): Int {
        return if (deviceInfo?.fwFamily == "D" &&  firmwareVersion <= Version("2.3.8")) {
            1
        } else if (firmwareVersion <= Version("2.4.6")) {
            2
        } else {
            3
        }
    }

    override fun getLedLayoutResponse(): LedLayout? {
        return get<LedLayout>("$baseUrl/led/layout/full")
    }

    override fun ledReset() {
        refreshTokenIfNeeded()
        get<String>("$baseUrl/led/reset")
    }

    fun getMusicStats(): MusicStats? {
        refreshTokenIfNeeded()
        return get<MusicStats>(
            url = "$baseUrl/music/stats",
        )
    }

    fun getMusicEnabled(): MusicEnabled? {
        refreshTokenIfNeeded()
        return get<MusicEnabled>(
            url = "$baseUrl/music/enabled",
        )
    }

    fun getMusicDriversCurrent(): MusicDriversCurrent? {
        refreshTokenIfNeeded()
        return get<MusicDriversCurrent>(
            url = "$baseUrl/music/drivers/current",
        )
    }

    fun getMusicDriversSets(): MusicDriversSets? {
        refreshTokenIfNeeded()
        return get<MusicDriversSets>(
            url = "$baseUrl/music/drivers/sets",
        )
    }

    fun getCurrentMusicDriversSet(): CurrentMusicDriverSet? {
        refreshTokenIfNeeded()
        return get<CurrentMusicDriverSet>(
            url = "$baseUrl/music/drivers/sets/current",
        )
    }

    fun getBrightness(): Brightness? {
        refreshTokenIfNeeded()
        return get<Brightness>(
            url = "$baseUrl/led/out/brightness",
        )
    }

    override fun setBrightness(brightness: Float) {
        refreshTokenIfNeeded()
        post<JsonObject>(
            url = "$baseUrl/led/out/brightness",
            body = Brightness(value = (100 * brightness).roundToInt()).writeValueAssBytes(),
            headers = mutableMapOf(
                CONTENT_TYPE to APPLICATION_JSON
            )
        )
    }

    fun getSaturation(): Saturation? {
        refreshTokenIfNeeded()
        return get<Saturation>(
            url = "$baseUrl/led/out/saturation",
        )
    }

    override fun setSaturation(saturation: Float) {
        refreshTokenIfNeeded()
        post<JsonObject>(
            url = "$baseUrl/led/out/saturation",
            body = Saturation(value = (100 * saturation).roundToInt()).writeValueAssBytes(),
            headers = mutableMapOf(
                CONTENT_TYPE to APPLICATION_JSON
            )
        )
    }

    fun getColor(): Color<*> {
        refreshTokenIfNeeded()
        val response = get<Map<String, Any>>(
            url = "$baseUrl/led/color",
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

    override fun setColor(color: Color<*>) {
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
                CONTENT_TYPE to APPLICATION_JSON
            )
        )
    }

    fun getConfig(): LedConfig? {
        refreshTokenIfNeeded()
        return get<LedConfig>(
            url = "$baseUrl/led/config",
        )
    }

    fun getEffects(): Effects? {
        return get<Effects>(
            url = "$baseUrl/led/effects",
        )
    }

    fun getEffectsCurrent(): EffectsCurrent? {
        return get<EffectsCurrent>(
            url = "$baseUrl/led/effects/current",
        )
    }

    fun getMovies(): Movies? {
        return get<Movies>(
            url = "$baseUrl/movies",
        )
    }

    fun deleteMovies(): JsonObject? {
        return delete<JsonObject>(
            url = "$baseUrl/movies",
        )
    }

    fun getCurrentMovie(): CurrentMovieResponse? {
        return get<CurrentMovieResponse>(
            url = "$baseUrl/movies/current",
        )
    }

    fun setCurrentMovie(currentMovieRequest: CurrentMovieRequest): JsonObject? {
        return post<JsonObject>(
            url = "$baseUrl/movies/current",
            body = currentMovieRequest.writeValueAssBytes(),
            headers = mutableMapOf(
                CONTENT_TYPE to APPLICATION_JSON
            )
        )
    }

    fun getPlaylist(): PlayList? {
        return get<PlayList>(
            url = "$baseUrl/playlist",
        )
    }

    fun getPlaylistCurrent(): String? {
        return get<String>(
            url = "$baseUrl/playlist/current",
        )
    }

    fun showFrame(
        name: String,
        frame: XledFrame
    ) {
        showSequence(name, XledSequence(frames = mutableListOf(frame)), 1)
    }

    /**
     * Experimental code which tries to upload a new movie and plays it in device.
     * Seems to overwrite the current sequence which is active in the device.
     */
    fun showSequence(
        name: String,
        sequence: XledSequence,
        fps: Int
    ) {
        setColor(RGBColor(0, 0, 0))
        setMode(DeviceMode.color)
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
            MovieConfig(
                frameDelay = 1000 / fps,
                ledsNumber = bytesPerLed,
                framesNumber = numberOfFrames,
            )
        )
        setCurrentMovie(CurrentMovieRequest(id = newMovie?.id))

        setMode(DeviceMode.movie)
    }

    override fun showRealTimeFrame(frame: XledFrame) {
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

    fun getLedMovieConfig(): MovieConfig? {
        return get<MovieConfig>(
            url = "$baseUrl/led/movie/config",
        )
    }

    fun setLedMovieConfig(movieConfig: MovieConfig): JsonObject? {
        return post<JsonObject>(
            url = "$baseUrl/led/movie/config",
            body = movieConfig.writeValueAssBytes(),
            headers = mutableMapOf(
                CONTENT_TYPE to APPLICATION_JSON
            )
        )
    }

    fun uploadNewMovie(newMovie: NewMovieRequest): NewMovieResponse? {
        return post<NewMovieResponse>(
            url = "$baseUrl/movies/new",
            body = newMovie.writeValueAssBytes(),
            headers = mutableMapOf(
                CONTENT_TYPE to APPLICATION_JSON
            )
        )
    }

    fun uploadNewMovieToListOfMovies(frame: XledFrame): Movie? {
        val bytes = frame.toByteArray(bytesPerLed)
        return uploadNewMovieToListOfMovies(bytes)
    }

    fun uploadNewMovieToListOfMovies(bytes: ByteArray): Movie? {
        return post<Movie>(
            url = "$baseUrl/led/movie/full",
            body = bytes,
            headers = mutableMapOf(
                CONTENT_TYPE to "application/octet-stream"
            )
        )
    }

    override fun getTimer(): Timer? {
        return get<Timer>(
            url = "$baseUrl/timer"
        )
    }

    override fun setTimer(
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

    override fun setTimer(
        timeOnHour: Int,
        timeOnMinute: Int,
        timeOffHour: Int,
        timeOffMinute: Int
    ): Timer? {
        val timer = Timer(
            timeNow = TimeUtil.utcSecondsAfterMidnight(),
            timeOn =  TimeUtil.utcSecondsAfterMidnight(timeOnHour, timeOnMinute),
            timeOff =  TimeUtil.utcSecondsAfterMidnight(timeOffHour, timeOffMinute)
        )
        return setTimer(timer)
    }

    override fun setTimer(timer: Timer): Timer? {
        val result = post<JsonObject>(
            url = "$baseUrl/timer",
            body = timer.writeValueAssBytes(),
            headers = mutableMapOf(
                CONTENT_TYPE to APPLICATION_JSON
            )
        )
        return if (result?.responseCode == ResponseCode.Ok) {
            getTimer()
        } else {
            log.warn("Could not set timer")
            null
        }
    }
}
