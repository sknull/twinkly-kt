package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.Session
import de.visualdigits.kotlin.twinkly.model.device.UDP_PORT_STREAMING
import de.visualdigits.kotlin.twinkly.model.device.xled.request.MoviesCurrentRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.request.NewMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.CurrentMusicDriverSet
import de.visualdigits.kotlin.twinkly.model.device.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Effects
import de.visualdigits.kotlin.twinkly.model.device.xled.response.EffectsCurrent
import de.visualdigits.kotlin.twinkly.model.device.xled.response.LedConfig
import de.visualdigits.kotlin.twinkly.model.device.xled.response.MovieConfig
import de.visualdigits.kotlin.twinkly.model.device.xled.response.MoviesCurrentResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.MusicDriversCurrent
import de.visualdigits.kotlin.twinkly.model.device.xled.response.MusicStats
import de.visualdigits.kotlin.twinkly.model.device.xled.response.NewMovieResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.PlayList
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
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
import java.time.OffsetDateTime
import java.util.Base64
import kotlin.math.min
import kotlin.math.roundToInt

private const val CONTENT_TYPE = "Content-Type"
private const val APPLICATION_JSON = "application/json"

class XLedDevice(
    host: String = "",
    override var width: Int = 0,
    override var height: Int = 0
): XLed, Session(
    host,
    "http://$host/xled/v1",
) {

    val deviceInfo: DeviceInfo?
    val ledLayout: LedLayout?

    override val bytesPerLed: Int

    init {
        if (host.isNotEmpty()) {
            // ensure we are logged in up to here to avoid unneeded requests
            if (!tokens.containsKey(host)) login()
            if (tokens[host]?.loggedIn == true) {
                deviceInfo = deviceInfo()
                ledLayout = getLayout()
                bytesPerLed = deviceInfo?.bytesPerLed?:4 // fallback to rgbw assuming gen 2 device
            } else {
                deviceInfo = null
                ledLayout = null
                bytesPerLed = 0
            }
        } else {
            deviceInfo = DeviceInfo()
            ledLayout = LedLayout()
            bytesPerLed = 0
        }
    }

    override fun powerOn() {
        refreshTokenIfNeeded()
        listOf(DeviceMode.playlist, DeviceMode.movie, DeviceMode.effect)
            .forEach { mode ->
                val responseCode = setMode(mode)
                if (responseCode?.responseCode == ResponseCode.Ok) {
                    return
                }
            }
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
        log.debug("Setting mode for device '$host' to ${mode.name}...")
        return post<Mode>(
            url = "$baseUrl/led/mode",
            body = body.toByteArray(),
            headers = mutableMapOf(
                CONTENT_TYPE to APPLICATION_JSON
            )
        )
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
            body = Brightness(value = (100 * brightness).roundToInt()).marshallToBytes(),
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
            body = Saturation(value = (100 * saturation).roundToInt()).marshallToBytes(),
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
                val rgbColor = color.toRGB()
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

    fun getLayout(): LedLayout? {
        return get<LedLayout>(
            url = "$baseUrl/led/layout/full",
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

    fun getMoviesCurrent(): MoviesCurrentResponse? {
        return get<MoviesCurrentResponse>(
            url = "$baseUrl/movies/current",
        )
    }

    fun setMoviesCurrent(moviesCurrentRequest: MoviesCurrentRequest): JsonObject? {
        return post<JsonObject>(
            url = "$baseUrl/movies/current",
            body = moviesCurrentRequest.marshallToBytes(),
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

    fun showSequence(
        name: String,
        sequence: XledSequence,
        fps: Int
    ) {
        setColor(RGBColor(0, 0, 0))
        setMode(DeviceMode.color)
        deleteMovies()

        val numberOfFrames = sequence.size

        val newMovie = newMovie(
            NewMovieRequest(
                name = name.substring(0, min(name.length, 32)),
                descriptorType = "rgbw_raw",
                ledsPerFrame = bytesPerLed,
                framesNumber = numberOfFrames,
                fps = fps
            )
        )
        uploadMovie(sequence.toByteArray(bytesPerLed))
        movieConfig(
            MovieConfig(
                frameDelay = 1000 / fps,
                ledsNumber = bytesPerLed,
                framesNumber = numberOfFrames,
            )
        )
        setMoviesCurrent(MoviesCurrentRequest(id = newMovie?.id))

        setMode(DeviceMode.movie)
    }

    override fun showRealTimeFrame(frame: XledFrame) {
        UdpClient(host, UDP_PORT_STREAMING).use { udpClient ->
            frame.toByteArray(bytesPerLed)
                .toList()
                .chunked(900)
                .mapIndexed { index, value ->
                    udpClient.send(byteArrayOf(0x03) +
                        Base64.getDecoder().decode(tokens[host]?.authToken?:"") +
                        byteArrayOf(0x00, 0x00) +
                        byteArrayOf(index.toByte()) +
                        value.toByteArray()
                    )
                }
        }
    }

    fun uploadMovie(frame: XledFrame): Movie? {
        val bytes = frame.toByteArray(bytesPerLed)
        return uploadMovie(bytes)
    }

    fun uploadMovie(bytes: ByteArray): Movie? {
        return post<Movie>(
            url = "$baseUrl/led/movie/full",
            body = bytes,
            headers = mutableMapOf(
                CONTENT_TYPE to "application/octet-stream"
            )
        )
    }

    fun movieConfig(): MovieConfig? {
        return get<MovieConfig>(
            url = "$baseUrl/led/movie/config",
        )
    }

    fun movieConfig(movieConfig: MovieConfig): JsonObject? {
        return post<JsonObject>(
            url = "$baseUrl/led/movie/config",
            body = movieConfig.marshallToBytes(),
            headers = mutableMapOf(
                CONTENT_TYPE to APPLICATION_JSON
            )
        )
    }

    fun newMovie(newMovie: NewMovieRequest): NewMovieResponse? {
        return post<NewMovieResponse>(
            url = "$baseUrl/movies/new",
            body = newMovie.marshallToBytes(),
            headers = mutableMapOf(
                CONTENT_TYPE to APPLICATION_JSON
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
            body = timer.marshallToBytes(),
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
