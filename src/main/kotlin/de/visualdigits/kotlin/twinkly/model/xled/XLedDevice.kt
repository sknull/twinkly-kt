package de.visualdigits.kotlin.twinkly.model.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.frame.XledSequence
import de.visualdigits.kotlin.twinkly.model.Session
import de.visualdigits.kotlin.twinkly.model.UDP_PORT_STREAMING
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.xled.request.MoviesCurrentRequest
import de.visualdigits.kotlin.twinkly.model.xled.request.NewMovieRequest
import de.visualdigits.kotlin.twinkly.model.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.xled.response.CurrentMusicDriverSet
import de.visualdigits.kotlin.twinkly.model.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.xled.response.Effects
import de.visualdigits.kotlin.twinkly.model.xled.response.EffectsCurrent
import de.visualdigits.kotlin.twinkly.model.xled.response.LedConfig
import de.visualdigits.kotlin.twinkly.model.xled.response.MovieConfig
import de.visualdigits.kotlin.twinkly.model.xled.response.MoviesCurrentResponse
import de.visualdigits.kotlin.twinkly.model.xled.response.MusicDriversCurrent
import de.visualdigits.kotlin.twinkly.model.xled.response.MusicStats
import de.visualdigits.kotlin.twinkly.model.xled.response.NewMovieResponse
import de.visualdigits.kotlin.twinkly.model.xled.response.PlayList
import de.visualdigits.kotlin.twinkly.model.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.xled.response.ledlayout.LedLayout
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.Mode
import de.visualdigits.kotlin.twinkly.model.xled.response.movies.Movie
import de.visualdigits.kotlin.twinkly.model.xled.response.movies.Movies
import de.visualdigits.kotlin.twinkly.model.xled.response.musicdriverssets.MusicDriversSets
import de.visualdigits.kotlin.twinkly.model.xled.response.musicenabled.MusicEnabled
import de.visualdigits.kotlin.twinkly.model.xled.response.ResponseCode
import de.visualdigits.kotlin.udp.UdpClient
import java.util.Base64
import kotlin.math.min

class XLedDevice(host: String): XLed, Session(
    host,
    "http://$host/xled/v1"
) {

    val deviceInfo: DeviceInfo
    val layout: LedLayout

    override val columns: Int
    override val rows: Int
    override val bytesPerLed: Int

    init {
        login()
        deviceInfo = deviceInfo()
        layout = layout()
        columns = layout.columns
        rows = layout.rows
        bytesPerLed = deviceInfo.bytesPerLed!!
    }

    override fun powerOn() {
        listOf(DeviceMode.playlist, DeviceMode.movie, DeviceMode.effect)
            .forEach { mode ->
                val responseCode = mode(mode)
                if (responseCode.responseCode == ResponseCode.Ok) {
                    return
                }
            }
    }

    override fun powerOff() {
        mode(DeviceMode.off)
    }

    fun mode(): DeviceMode {
        val response = get<Mode>(
            url = "$baseUrl/led/mode",
        )
        return response.deviceMode
    }

    override fun mode(mode: DeviceMode): JsonObject {
        log.info("#### Setting mode to $mode...")
        return post<Mode>(
            url = "$baseUrl/led/mode",
            body = "{\"mode\":\"$mode\"}".toByteArray(),
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    override fun ledReset() {
        return get("$baseUrl/led/reset")
    }

    fun musicStats(): MusicStats {
        return get<MusicStats>(
            url = "$baseUrl/music/stats",
        )
    }

    fun musicEnabled(): MusicEnabled {
        return get<MusicEnabled>(
            url = "$baseUrl/music/enabled",
        )
    }

    fun musicDriversCurrent(): MusicDriversCurrent {
        return get<MusicDriversCurrent>(
            url = "$baseUrl/music/drivers/current",
        )
    }

    fun musicDriversSets(): MusicDriversSets {
        return get<MusicDriversSets>(
            url = "$baseUrl/music/drivers/sets",
        )
    }

    fun currentMusicDriversSet(): CurrentMusicDriverSet {
        return get<CurrentMusicDriverSet>(
            url = "$baseUrl/music/drivers/sets/current",
        )
    }

    fun brightness(): Brightness {
        return get<Brightness>(
            url = "$baseUrl/led/out/brightness",
        )
    }

    override fun brightness(brightness: Brightness) {
        post<JsonObject>(
            url = "$baseUrl/led/out/brightness",
            body = brightness.marshallToBytes(),
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    fun saturation(): Saturation {
        return get<Saturation>(
            url = "$baseUrl/led/out/saturation",
        )
    }

    override fun saturation(saturation: Saturation) {
        post<JsonObject>(
            url = "$baseUrl/led/out/saturation",
            body = saturation.marshallToBytes(),
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    fun color(): Color<*> {
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

    override fun color(color: Color<*>) {
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
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    fun config(): LedConfig {
        return get<LedConfig>(
            url = "$baseUrl/led/config",
        )
    }

    fun layout(): LedLayout {
        return get<LedLayout>(
            url = "$baseUrl/led/layout/full",
        )
    }

    fun effects(): Effects {
        return get<Effects>(
            url = "$baseUrl/led/effects",
        )
    }

    fun effectsCurrent(): EffectsCurrent {
        return get<EffectsCurrent>(
            url = "$baseUrl/led/effects/current",
        )
    }

    fun movies(): Movies {
        return get<Movies>(
            url = "$baseUrl/movies",
        )
    }

    fun deleteMovies(): JsonObject {
        return delete<JsonObject>(
            url = "$baseUrl/movies",
        )
    }

    fun moviesCurrent(): MoviesCurrentResponse {
        return get<MoviesCurrentResponse>(
            url = "$baseUrl/movies/current",
        )
    }

    fun moviesCurrent(moviesCurrentRequest: MoviesCurrentRequest): JsonObject {
        return post<JsonObject>(
            url = "$baseUrl/movies/current",
            body = moviesCurrentRequest.marshallToBytes(),
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    fun playlist(): PlayList {
        return get<PlayList>(
            url = "$baseUrl/playlist",
        )
    }

    fun playlistCurrent(): String {
        return get<String>(
            url = "$baseUrl/playlist/current",
        )
    }

    fun showFrame(
        name: String,
        frame: XledFrame
    ) {
        showSequence(name, XledSequence(mutableListOf(frame)), 1)
    }

    fun showSequence(
        name: String,
        sequence: XledSequence,
        fps: Int
    ) {
        color(RGBColor(0, 0, 0))
        mode(DeviceMode.color)
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
        uploadMovie(sequence.toByteArray())
        movieConfig(
            MovieConfig(
                frameDelay = 1000 / fps,
                ledsNumber = numberOfLed,
                framesNumber = numberOfFrames,
            )
        )
        moviesCurrent(MoviesCurrentRequest(id = newMovie.id))

        mode(DeviceMode.movie)
    }

    override fun showRealTimeFrame(frame: XledFrame) {
        UdpClient(host, UDP_PORT_STREAMING).use { udpClient ->
            frame.toByteArray()
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
        val bytes = frame.toByteArray()
        return uploadMovie(bytes)
    }

    fun uploadMovie(bytes: ByteArray): Movie {
        return post<Movie>(
            url = "$baseUrl/led/movie/full",
            body = bytes,
            headers = mapOf(
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
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )
    }

    fun newMovie(newMovie: NewMovieRequest): NewMovieResponse {
        return post<NewMovieResponse>(
            url = "$baseUrl/movies/new",
            body = newMovie.marshallToBytes(),
            headers = mapOf(
                "Content-Type" to "application/json"
            )
        )
    }
}
