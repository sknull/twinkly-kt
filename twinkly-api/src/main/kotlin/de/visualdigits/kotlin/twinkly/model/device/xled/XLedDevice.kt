package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
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
import de.visualdigits.kotlin.util.TimeUtil
import java.time.OffsetDateTime
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Base class for specific twinkly devices.
 * Handles login and out.
 */
open class XLedDevice protected constructor(
    ipAddress: String,
    override var name: String,
    override var width: Int = 0,
    override var height: Int = 0,
    transformation: ((XledFrame) -> XledFrame)? = null
) : AbstractXled(
    ipAddress = ipAddress,
    baseUrl = "http://$ipAddress/xled/v1",
    transformation = transformation
), XLed {

    companion object {

        private val cache = mutableMapOf<String, XLedDevice>()

        fun instance(
            ipAddress: String,
            name: String = "",
            width: Int = 0,
            height: Int = 0,
            transformation: ((XledFrame) -> XledFrame)? = null
        ): XLedDevice {
            return cache.computeIfAbsent(ipAddress) {
                XLedDevice(
                    ipAddress,
                    name,
                    width,
                    height,
                    transformation
                )
            }
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
            clazz = Mode::class.java
        )
    }

    override fun getLedMode(): LedMode? {
        refreshTokenIfNeeded()
        return getMode()?.ledMode
    }

    override fun setLedMode(ledMode: LedMode): JsonObject? {
        refreshTokenIfNeeded()
        val body = "{\"mode\":\"${ledMode.name}\"}"
        log.debug("Setting mode for device '${getIpAddress()}' to ${ledMode.name}...")
        return post<Mode>(
            url = "http://${getIpAddress()}/xled/v1/led/mode",
            body = body.toByteArray(),
            headers = mutableMapOf(
                "Content-Type" to "application/json"
            ),
            clazz = Mode::class.java
        )
    }

    override fun ledReset() {
        refreshTokenIfNeeded()
        get<String>(
            url = "$baseUrl/led/reset",
            clazz = String::class.java)
    }

    override fun getMusicEffects(): MusicEffectsResponse? {
        refreshTokenIfNeeded()
        return get<MusicEffectsResponse>(
            url = "$baseUrl/music/effects",
            clazz = MusicEffectsResponse::class.java
        )
    }

    override fun getCurrentMusicEffect(): CurrentMusicEffectResponse? {
        refreshTokenIfNeeded()
        return get<CurrentMusicEffectResponse>(
            url = "$baseUrl/music/effects/current",
            clazz = CurrentMusicEffectResponse::class.java
        )
    }

    override fun setCurrentMusicEffect(effectId: String): JsonObject? {
        refreshTokenIfNeeded()
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

    override fun getMusicConfig(): MusicConfig? {
        refreshTokenIfNeeded()
        val response = get<MusicConfig>(
            url = "$baseUrl/music/config",
            clazz = MusicConfig::class.java
        )
        return response
    }

    override fun getLedMusicStats(): LedMusicStatsResponse? {
        refreshTokenIfNeeded()
        return get<LedMusicStatsResponse>(
            url = "$baseUrl/music/stats",
            clazz = LedMusicStatsResponse::class.java
        )
    }

    override fun getMusicEnabled(): MusicEnabledResponse? {
        refreshTokenIfNeeded()
        return get<MusicEnabledResponse>(
            url = "$baseUrl/music/enabled",
            clazz = MusicEnabledResponse::class.java
        )
    }

    override fun setMusicEnabled(enabled: Boolean): JsonObject? {
        refreshTokenIfNeeded()
        return post<JsonObject>(
            url = "$baseUrl/music/enabled",
            body = "{\"enabled\":${if (enabled) 1 else 0}}".toByteArray(),
            clazz = JsonObject::class.java
        )
    }

    override fun getMusicDriversCurrent(): CurrentMusicDriversResponse? {
        refreshTokenIfNeeded()
        return get<CurrentMusicDriversResponse>(
            url = "$baseUrl/music/drivers/current",
            clazz = CurrentMusicDriversResponse::class.java
        )
    }

    override fun getMusicDriversSets(): MusicDriversSets? {
        refreshTokenIfNeeded()
        return get<MusicDriversSets>(
            url = "$baseUrl/music/drivers/sets",
            clazz = MusicDriversSets::class.java
        )
    }

    override fun getCurrentMusicDriversSet(): CurrentMusicDriverSetResponse? {
        refreshTokenIfNeeded()
        return get<CurrentMusicDriverSetResponse>(
            url = "$baseUrl/music/drivers/sets/current",
            clazz = CurrentMusicDriverSetResponse::class.java
        )
    }

    override fun getBrightness(): Brightness? {
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

    override fun getSaturation(): Saturation? {
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

    override fun getColor(): Color<*> {
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

    override fun getLedConfig(): LedConfigResponse? {
        refreshTokenIfNeeded()
        return get<LedConfigResponse>(
            url = "$baseUrl/led/config",
            clazz = LedConfigResponse::class.java
        )
    }

    override fun getLedEffects(): LedEffectsResponse? {
        refreshTokenIfNeeded()
        return get<LedEffectsResponse>(
            url = "$baseUrl/led/effects",
            clazz = LedEffectsResponse::class.java
        )
    }

    override fun getCurrentLedEffect(): CurrentLedEffectResponse? {
        refreshTokenIfNeeded()
        return get<CurrentLedEffectResponse>(
            url = "$baseUrl/led/effects/current",
            clazz = CurrentLedEffectResponse::class.java
        )
    }

    override fun setCurrentLedEffect(effectId: String): JsonObject? {
        refreshTokenIfNeeded()
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

    override fun getMovies(): Movies? {
        refreshTokenIfNeeded()
        return get<Movies>(
            url = "$baseUrl/movies",
            clazz = Movies::class.java
        )
    }

    override fun deleteMovies(): JsonObject? {
        refreshTokenIfNeeded()
        return delete<JsonObject>(
            url = "$baseUrl/movies",
            clazz = JsonObject::class.java
        )
    }

    override fun getCurrentMovie(): CurrentMovieResponse? {
        refreshTokenIfNeeded()
        return get<CurrentMovieResponse>(
            url = "$baseUrl/movies/current",
            clazz = CurrentMovieResponse::class.java
        )
    }

    override fun setCurrentMovie(currentMovieRequest: CurrentMovieRequest): JsonObject? {
        refreshTokenIfNeeded()
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

    override fun getPlaylist(): PlayList? {
        refreshTokenIfNeeded()
        return get<PlayList>(
            url = "$baseUrl/playlist",
            clazz = PlayList::class.java
        )
    }

    override fun getCurrentPlaylist(): String? {
        refreshTokenIfNeeded()
        return get<String>(
            url = "$baseUrl/playlist/current",
            clazz = String::class.java
        )
    }

    override fun showFrame(
        name: String,
        frame: XledFrame
    ) {
        showSequence(name, XledSequence(frames = mutableListOf(frame)), 1)
    }

    /**
     * Experimental code which tries to upload a new movie and plays it in device.
     * Seems to overwrite the current sequence which is active in the device.
     */
    override fun showSequence(
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

    override fun setLedMovieConfig(movieConfig: LedMovieConfigResponse): JsonObject? {
        refreshTokenIfNeeded()
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

    override fun uploadNewMovie(newMovie: NewMovieRequest): NewMovieResponse? {
        refreshTokenIfNeeded()
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

    override fun uploadNewMovieToListOfMovies(frame: XledFrame): Movie? {
        refreshTokenIfNeeded()
        val bytes = frame.toByteArray(bytesPerLed)
        return uploadNewMovieToListOfMovies(bytes)
    }

    override fun uploadNewMovieToListOfMovies(bytes: ByteArray): Movie? {
        refreshTokenIfNeeded()
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
        refreshTokenIfNeeded()
        return get<Timer>(
            url = "$baseUrl/timer",
            clazz = Timer::class.java
        )
    }

    override fun setTimer(
        timeOn: OffsetDateTime,
        timeOff: OffsetDateTime
    ): Timer? {
        refreshTokenIfNeeded()
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
            timeOn = TimeUtil.utcSecondsAfterMidnight(timeOnHour, timeOnMinute),
            timeOff = TimeUtil.utcSecondsAfterMidnight(timeOffHour, timeOffMinute)
        )
        return setTimer(timer)
    }

    override fun setTimer(timer: Timer): Timer? {
        refreshTokenIfNeeded()
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

    /**
     * Returns the devices group infos.
     */
    fun getGroupStatus(): String? {
        refreshTokenIfNeeded()
        return get<String>(
            url = "$baseUrl/group/status",
            clazz = String::class.java
        )
    }
}
