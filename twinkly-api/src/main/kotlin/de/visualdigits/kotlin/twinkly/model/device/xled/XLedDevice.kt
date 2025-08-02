package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.Session
import de.visualdigits.kotlin.twinkly.model.device.xled.request.CurrentMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.request.NewMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.PlayList
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.CurrentLedEffectResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedConfigResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedEffectsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedLayout
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.LedMode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.Mode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.CurrentMovieResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.Movie
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.MovieConfigResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.Movies
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.NewMovieResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicDriverSetResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicDriversResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicEffectResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicDriversSets
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicEffectsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicEnabledResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicStatsResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MusicConfigResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.musicmode.MusicModeResponse
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import de.visualdigits.kotlin.udp.UdpClient
import de.visualdigits.kotlin.util.TimeUtil
import java.time.OffsetDateTime
import java.util.Base64
import kotlin.math.min
import kotlin.math.roundToInt

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

    val ledLayout: LedLayout?

    override val bytesPerLed: Int

    init {
        if (ipAddress.isNotEmpty()) {
            if (tokens[ipAddress]?.loggedIn == true) {
                ledLayout = getLedLayoutResponse()
                bytesPerLed = deviceInfo?.bytesPerLed?:3
            } else {
                ledLayout = null
                bytesPerLed = 0
            }
        } else {
            ledLayout = null
            bytesPerLed = 0
        }
    }

    override fun powerOn() {
        refreshTokenIfNeeded()
        // try modes until it works...
        listOf(LedMode.playlist, LedMode.movie, LedMode.effect)
            .find { mode -> setLedMode(mode)?.responseCode == ResponseCode.Ok }
    }

    override fun powerOff() {
        refreshTokenIfNeeded()
        setLedMode(LedMode.off)
    }

    override fun getMode(): Mode? {
        refreshTokenIfNeeded()
        return get<Mode>(
            url = "$baseUrl/led/mode",
            clazz =Mode::class.java
        )
    }

    override fun getDeviceMode(): LedMode? {
        return getMode()?.ledMode
    }

    override fun setLedMode(ledMode: LedMode): JsonObject? {
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

    override fun getLedLayoutResponse(): LedLayout? {
        return get<LedLayout>("$baseUrl/led/layout/full",
            clazz = LedLayout::class.java)
    }

    override fun ledReset() {
        refreshTokenIfNeeded()
        get<String>("$baseUrl/led/reset",
            clazz = String::class.java)
    }

    fun getMusicEffects(): MusicEffectsResponse? {
        return get<MusicEffectsResponse>(
            url = "$baseUrl/music/effects",
            clazz = MusicEffectsResponse::class.java
        )
    }

    fun getCurrentMusicEffect(): CurrentMusicEffectResponse? {
        return get<CurrentMusicEffectResponse>(
            url = "$baseUrl/music/effects/current",
            clazz = CurrentMusicEffectResponse::class.java
        )
    }

    fun setCurrentMusicEffect(effectId: String): JsonObject? {
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

    fun getMusicConfig(): MusicConfigResponse? {
        val response = get<MusicConfigResponse>(
            url = "$baseUrl/music/config",
            clazz = MusicConfigResponse::class.java
        )
        return response
    }

    fun getMusicStats(): MusicStatsResponse? {
        refreshTokenIfNeeded()
        return get<MusicStatsResponse>(
            url = "$baseUrl/music/stats",
            clazz = MusicStatsResponse::class.java
        )
    }

    fun getMusicEnabled(): MusicEnabledResponse? {
        refreshTokenIfNeeded()
        return get<MusicEnabledResponse>(
            url = "$baseUrl/music/enabled",
            clazz = MusicEnabledResponse::class.java
        )
    }

    fun setMusicEnabled(enabled: Boolean): JsonObject? {
        refreshTokenIfNeeded()
        return post<JsonObject>(
            url = "$baseUrl/music/enabled",
            body = "{\"enabled\":${if (enabled) 1 else 0}}".toByteArray(),
            clazz = JsonObject::class.java
        )
    }

    fun getMusicMode(): MusicModeResponse? {
        val response = get<MusicModeResponse>(
            url = "$baseUrl/music/mode",
            clazz = MusicModeResponse::class.java
        )
        return response
    }

    fun setMusicMode(musicMode: String): JsonObject? {
        refreshTokenIfNeeded()
        return post<JsonObject>(
            url = "$baseUrl/music/mode",
            body = "{\"effect\":\"$musicMode\"}".toByteArray(),
            clazz = JsonObject::class.java
        )
    }

    fun getMusicDriversCurrent(): CurrentMusicDriversResponse? {
        refreshTokenIfNeeded()
        return get<CurrentMusicDriversResponse>(
            url = "$baseUrl/music/drivers/current",
            clazz = CurrentMusicDriversResponse::class.java
        )
    }

    fun getMusicDriversSets(): MusicDriversSets? {
        refreshTokenIfNeeded()
        return get<MusicDriversSets>(
            url = "$baseUrl/music/drivers/sets",
            clazz = MusicDriversSets::class.java
        )
    }

    fun getCurrentMusicDriversSet(): CurrentMusicDriverSetResponse? {
        refreshTokenIfNeeded()
        return get<CurrentMusicDriverSetResponse>(
            url = "$baseUrl/music/drivers/sets/current",
            clazz = CurrentMusicDriverSetResponse::class.java
        )
    }

    fun getBrightness(): Brightness? {
        refreshTokenIfNeeded()
        return get<Brightness>(
            url = "$baseUrl/led/out/brightness",
            clazz = Brightness::class.java
        )
    }

    override fun setBrightness(brightness: Float) {
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

    fun getSaturation(): Saturation? {
        refreshTokenIfNeeded()
        return get<Saturation>(
            url = "$baseUrl/led/out/saturation",
            clazz = Saturation::class.java
        )
    }

    override fun setSaturation(saturation: Float) {
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

    fun getColor(): Color<*> {
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
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            clazz = JsonObject::class.java
        )
    }

    fun getLedConfig(): LedConfigResponse? {
        refreshTokenIfNeeded()
        return get<LedConfigResponse>(
            url = "$baseUrl/led/config",
            clazz = LedConfigResponse::class.java
        )
    }

    fun getLedEffects(): LedEffectsResponse? {
        return get<LedEffectsResponse>(
            url = "$baseUrl/led/effects",
            clazz = LedEffectsResponse::class.java
        )
    }

    fun getCurrentLedEffect(): CurrentLedEffectResponse? {
        return get<CurrentLedEffectResponse>(
            url = "$baseUrl/led/effects/current",
            clazz = CurrentLedEffectResponse::class.java
        )
    }

    fun setCurrentLedEffect(effectId: String): JsonObject? {
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

    fun getMovies(): Movies? {
        return get<Movies>(
            url = "$baseUrl/movies",
            clazz = Movies::class.java
        )
    }

    fun deleteMovies(): JsonObject? {
        return delete<JsonObject>(
            url = "$baseUrl/movies",
            clazz = JsonObject::class.java
        )
    }

    fun getCurrentMovie(): CurrentMovieResponse? {
        return get<CurrentMovieResponse>(
            url = "$baseUrl/movies/current",
            clazz = CurrentMovieResponse::class.java
        )
    }

    fun setCurrentMovie(currentMovieRequest: CurrentMovieRequest): JsonObject? {
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

    fun getPlaylist(): PlayList? {
        return get<PlayList>(
            url = "$baseUrl/playlist",
            clazz = PlayList::class.java
        )
    }

    fun getPlaylistCurrent(): String? {
        return get<String>(
            url = "$baseUrl/playlist/current",
            clazz = String::class.java
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
            MovieConfigResponse(
                frameDelay = 1000 / fps,
                ledsNumber = bytesPerLed,
                framesNumber = numberOfFrames,
            )
        )
        setCurrentMovie(CurrentMovieRequest(id = newMovie?.id))

        setLedMode(LedMode.movie)
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

    fun getLedMovieConfig(): MovieConfigResponse? {
        return get<MovieConfigResponse>(
            url = "$baseUrl/led/movie/config",
            clazz = MovieConfigResponse::class.java
        )
    }

    fun setLedMovieConfig(movieConfig: MovieConfigResponse): JsonObject? {
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

    fun uploadNewMovie(newMovie: NewMovieRequest): NewMovieResponse? {
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

    fun uploadNewMovieToListOfMovies(frame: XledFrame): Movie? {
        val bytes = frame.toByteArray(bytesPerLed)
        return uploadNewMovieToListOfMovies(bytes)
    }

    fun uploadNewMovieToListOfMovies(bytes: ByteArray): Movie? {
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

    override fun getTimer(): Timer? {
        return get<Timer>(
            url = "$baseUrl/timer",
            clazz = Timer::class.java
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
}
