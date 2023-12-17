package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.Session
import de.visualdigits.kotlin.twinkly.model.UDP_PORT_STREAMING
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
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
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.Base64
import kotlin.math.min

class XLedDevice(
    host: String,
    val deviceOrigin: DeviceOrigin
): XLed, Session(
    host,
    "http://$host/xled/v1"
) {

    val deviceInfo: DeviceInfo
    val ledLayout: LedLayout

    override val width: Int
    override val height: Int
    override val bytesPerLed: Int

    var tokenExpires: Long = 0

    init {
        if (host.isNotEmpty()) {
            tokenExpires = System.currentTimeMillis() + login() - 5000
            log.info("#### Token expires '${formatEpoch(tokenExpires)}'")
            deviceInfo = deviceInfo()
            ledLayout = getLayout()
            width = if (deviceOrigin.isPortrait()) ledLayout.columns else ledLayout.rows
            height = if (deviceOrigin.isPortrait()) ledLayout.rows else ledLayout.columns
            bytesPerLed = deviceInfo.bytesPerLed!!
        } else {
            deviceInfo = DeviceInfo()
            ledLayout = LedLayout()
            width = 0
            height = 0
            bytesPerLed = 0
        }
    }

    fun refreshTokenIfNeeded() {
        if (System.currentTimeMillis() > tokenExpires) {
            log.info("Refreshing token for device '$host'...")
            tokenExpires = System.currentTimeMillis() + login() - 5000
            log.info("#### Token expires '${formatEpoch(tokenExpires)}'")
        }
    }

    override fun powerOn() {
        refreshTokenIfNeeded()
        listOf(DeviceMode.playlist, DeviceMode.movie, DeviceMode.effect)
            .forEach { mode ->
                val responseCode = setMode(mode)
                if (responseCode.responseCode == ResponseCode.Ok) {
                    return
                }
            }
    }

    override fun powerOff() {
        refreshTokenIfNeeded()
        setMode(DeviceMode.off)
    }

    override fun getMode(): DeviceMode {
        refreshTokenIfNeeded()
        val response = get<Mode>(
            url = "$baseUrl/led/mode",
        )
        return response.deviceMode
    }

    override fun setMode(mode: DeviceMode): JsonObject {
        refreshTokenIfNeeded()
        val body = "{\"mode\":\"${mode.name}\"}"
        log.info("Setting mode for device '$host' to ${mode.name}...")
        return post<Mode>(
            url = "$baseUrl/led/mode",
            body = body.toByteArray(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    override fun ledReset() {
        refreshTokenIfNeeded()
        return get("$baseUrl/led/reset")
    }

    fun getMusicStats(): MusicStats {
        refreshTokenIfNeeded()
        return get<MusicStats>(
            url = "$baseUrl/music/stats",
        )
    }

    fun getMusicEnabled(): MusicEnabled {
        refreshTokenIfNeeded()
        return get<MusicEnabled>(
            url = "$baseUrl/music/enabled",
        )
    }

    fun getMusicDriversCurrent(): MusicDriversCurrent {
        refreshTokenIfNeeded()
        return get<MusicDriversCurrent>(
            url = "$baseUrl/music/drivers/current",
        )
    }

    fun getMusicDriversSets(): MusicDriversSets {
        refreshTokenIfNeeded()
        return get<MusicDriversSets>(
            url = "$baseUrl/music/drivers/sets",
        )
    }

    fun getCurrentMusicDriversSet(): CurrentMusicDriverSet {
        refreshTokenIfNeeded()
        return get<CurrentMusicDriverSet>(
            url = "$baseUrl/music/drivers/sets/current",
        )
    }

    fun getBrightness(): Brightness {
        refreshTokenIfNeeded()
        return get<Brightness>(
            url = "$baseUrl/led/out/brightness",
        )
    }

    override fun setBrightness(brightness: Brightness) {
        refreshTokenIfNeeded()
        post<JsonObject>(
            url = "$baseUrl/led/out/brightness",
            body = brightness.marshallToBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    fun getSaturation(): Saturation {
        refreshTokenIfNeeded()
        return get<Saturation>(
            url = "$baseUrl/led/out/saturation",
        )
    }

    override fun setSaturation(saturation: Saturation) {
        refreshTokenIfNeeded()
        post<JsonObject>(
            url = "$baseUrl/led/out/saturation",
            body = saturation.marshallToBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    fun getColor(): Color<*> {
        refreshTokenIfNeeded()
        val response = get<Map<String, Any>>(
            url = "$baseUrl/led/color",
        )
        return if (response["red"] as Int > 0 || response["green"] as Int > 0 || response["blue"] as Int > 0 || response["white"] as Int > 0) {
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
                "Content-Type" to "application/json"
            )
        )
    }

    fun getConfig(): LedConfig {
        refreshTokenIfNeeded()
        return get<LedConfig>(
            url = "$baseUrl/led/config",
        )
    }

    fun getLayout(): LedLayout {
        return get<LedLayout>(
            url = "$baseUrl/led/layout/full",
        )
    }

    fun getEffects(): Effects {
        return get<Effects>(
            url = "$baseUrl/led/effects",
        )
    }

    fun getEffectsCurrent(): EffectsCurrent {
        return get<EffectsCurrent>(
            url = "$baseUrl/led/effects/current",
        )
    }

    fun getMovies(): Movies {
        return get<Movies>(
            url = "$baseUrl/movies",
        )
    }

    fun deleteMovies(): JsonObject {
        return delete<JsonObject>(
            url = "$baseUrl/movies",
        )
    }

    fun getMoviesCurrent(): MoviesCurrentResponse {
        return get<MoviesCurrentResponse>(
            url = "$baseUrl/movies/current",
        )
    }

    fun setMoviesCurrent(moviesCurrentRequest: MoviesCurrentRequest): JsonObject {
        return post<JsonObject>(
            url = "$baseUrl/movies/current",
            body = moviesCurrentRequest.marshallToBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    fun getPlaylist(): PlayList {
        return get<PlayList>(
            url = "$baseUrl/playlist",
        )
    }

    fun getPlaylistCurrent(): String {
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

        val deviceInfo = deviceInfo()
        val numberOfLed = deviceInfo.numberOfLed

        val numberOfFrames = sequence.size

        val newMovie = newMovie(
            NewMovieRequest(
                name = name.substring(0, min(name.length, 32)),
                descriptorType = "rgbw_raw",
                ledsPerFrame = numberOfLed,
                framesNumber = numberOfFrames,
                fps = fps
            )
        )
        uploadMovie(sequence.toByteArray(bytesPerLed))
        movieConfig(
            MovieConfig(
                frameDelay = 1000 / fps,
                ledsNumber = numberOfLed,
                framesNumber = numberOfFrames,
            )
        )
        setMoviesCurrent(MoviesCurrentRequest(id = newMovie.id))

        setMode(DeviceMode.movie)
    }

    override fun showRealTimeFrame(frame: XledFrame) {
        val translatedFrame = when (deviceOrigin) {
            DeviceOrigin.TOP_LEFT -> frame
            DeviceOrigin.TOP_RIGHT -> frame.rotateLeft()
            DeviceOrigin.BOTTOM_LEFT -> frame.rotateRight()
            DeviceOrigin.BOTTOM_RIGHT -> frame.rotate180()
        }
        UdpClient(host, UDP_PORT_STREAMING).use { udpClient ->
            translatedFrame.toByteArray(bytesPerLed)
                .toList()
                .chunked(900)
                .mapIndexed { index, value ->
                    udpClient.send(byteArrayOf(0x03) +
                        Base64.getDecoder().decode(authToken) +
                        byteArrayOf(0x00, 0x00) +
                        byteArrayOf(index.toByte()) +
                        value.toByteArray()
                    )
                }
        }
    }

    fun uploadMovie(frame: XledFrame): Movie {
        val bytes = frame.toByteArray(bytesPerLed)
        return uploadMovie(bytes)
    }

    fun uploadMovie(bytes: ByteArray): Movie {
        return post<Movie>(
            url = "$baseUrl/led/movie/full",
            body = bytes,
            headers = mutableMapOf(
                "Content-Type" to "application/octet-stream"
            )
        )
    }

    fun movieConfig(): MovieConfig {
        return get<MovieConfig>(
            url = "$baseUrl/led/movie/config",
        )
    }

    fun movieConfig(movieConfig: MovieConfig): JsonObject {
        return post<JsonObject>(
            url = "$baseUrl/led/movie/config",
            body = movieConfig.marshallToBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    fun newMovie(newMovie: NewMovieRequest): NewMovieResponse {
        return post<NewMovieResponse>(
            url = "$baseUrl/movies/new",
            body = newMovie.marshallToBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    override fun getTimer(): Timer {
        return get<Timer>(
            url = "$baseUrl/timer"
        )
    }

    override fun setTimer(
        timeOn: OffsetDateTime,
        timeOff: OffsetDateTime
    ): Timer {
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
    ): Timer {
        val timer = Timer(
            timeNow = TimeUtil.utcSecondsAfterMidnight(),
            timeOn =  TimeUtil.utcSecondsAfterMidnight(timeOnHour, timeOnMinute),
            timeOff =  TimeUtil.utcSecondsAfterMidnight(timeOffHour, timeOffMinute)
        )
        return setTimer(timer)
    }

    override fun setTimer(timer: Timer): Timer {
        val result = post<JsonObject>(
            url = "$baseUrl/timer",
            body = timer.marshallToBytes(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            )
        )
        return if (result.responseCode == ResponseCode.Ok) {
            getTimer()
        }
        else {
            throw IllegalStateException("Could not set timer")
        }
    }
}
