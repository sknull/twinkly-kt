package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.xled.request.CurrentMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.request.NewMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.PlayList
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.CurrentLedEffectResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedConfigResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedEffectsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.LedMode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.Mode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.SyncMode
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
import java.time.OffsetDateTime

private const val NO_DEVICE = "No device"

class XLedArray private constructor(
    private var xLedDevices: MutableList<MutableList<XLed>> = mutableListOf(),
    val deviceOrigin: DeviceOrigin = DeviceOrigin.TOP_LEFT,
    override var name: String = "",
    override var width: Int = 0,
    override var height: Int = 0,
    transformation: ((XledFrame) -> XledFrame)? = null
) : AbstractXled(
    ipAddress = "",
    baseUrl = "",
    transformation = transformation
), XLed {

    val columns = xLedDevices.size
    val rows = if (xLedDevices.isNotEmpty()) xLedDevices.minOf { column -> column.size } else 0

    companion object {

        private val cache = mutableMapOf<List<String>, XLedArray>()

        fun instance(
            xLedDevices: MutableList<MutableList<XLed>> = mutableListOf(),
            deviceOrigin: DeviceOrigin = DeviceOrigin.TOP_LEFT,
            name: String = "",
            width: Int = 0,
            height: Int = 0,
            transformation: ((XledFrame) -> XledFrame)? = null
        ): XLedArray {
            val key = xLedDevices.flatMap { c -> c.map { r -> r.getIpAddress() } }
            return cache.computeIfAbsent(key) {
                XLedArray(
                    xLedDevices,
                    deviceOrigin,
                    name = name,
                    width,
                    height,
                    transformation
                )
            }
        }
    }

    init {
        initialize()
    }

    private fun initialize() {
        if (xLedDevices.isNotEmpty()) {
            if (deviceOrigin.isPortrait()) {
                width = (0 until rows).maxOf { y -> (0 until columns).sumOf { x -> this[x, y].width } }
                height = (0 until columns).maxOf { x -> xLedDevices[x].sumOf { row -> row.height } }
            } else {
                width = (0 until columns).maxOf { x -> xLedDevices[x].sumOf { row -> row.height } }
                height = (0 until rows).maxOf { y -> (0 until columns).sumOf { x -> this[x, y].width } }
            }
        }
    }

    override fun isLoggedIn(): Boolean = xLedDevices.all { column -> column.all { device -> device.isLoggedIn() } }

    operator fun set(x: Int, y: Int, device: XLed) {
        xLedDevices[y][x] = device
    }

    operator fun get(x: Int, y: Int): XLed {
        return xLedDevices[y][x]
    }

    override fun logout() {
        xLedDevices.flatten().forEach { it.logout() }
    }

    override fun powerOn() {
        xLedDevices.flatten().forEach { it.powerOn() }
    }

    override fun powerOff() {
        xLedDevices.flatten().forEach { it.powerOff() }
    }

    fun getMasterDevice(): XLed? {
        return xLedDevices
            .flatten()
            .find { d -> d.getLedMovieConfig()?.sync?.mode == SyncMode.master }
            ?:xLedDevices.flatten().firstOrNull()
    }

    override fun ledReset() {
        xLedDevices.flatten().forEach { it.ledReset() }
    }

    override fun getBrightness(): Brightness? {
        return getMasterDevice()?.getBrightness()
    }

    override fun getMode(): Mode? {
        return getMasterDevice()?.getMode()
    }

    override fun getLedMode(): LedMode? {
        return getMasterDevice()?.getLedMode()
    }

    override fun setLedMode(mode: LedMode): JsonObject? {
        return xLedDevices.flatten().map { it.setLedMode(mode) }.firstOrNull()
    }

    override fun getLedEffects(): LedEffectsResponse? {
        return getMasterDevice()?.getLedEffects()
    }

    override fun getCurrentLedEffect(): CurrentLedEffectResponse? {
        return getMasterDevice()?.getCurrentLedEffect()
    }

    override fun setCurrentLedEffect(effectId: String): JsonObject? {
        return xLedDevices.flatten()
            .map { it.setCurrentLedEffect(effectId) }
            .firstOrNull()
    }

    override fun getMovies(): Movies? {
        return getMasterDevice()?.getMovies()
    }

    override fun deleteMovies(): JsonObject? {
        return getMasterDevice()?.deleteMovies()
    }

    override fun getCurrentMovie(): CurrentMovieResponse? {
        return getMasterDevice()?.getCurrentMovie()
    }

    override fun setCurrentMovie(currentMovieRequest: CurrentMovieRequest): JsonObject? {
        return getMasterDevice()?.setCurrentMovie(currentMovieRequest)
    }

    override fun getPlaylist(): PlayList? {
        return getMasterDevice()?.getPlaylist()
    }

    override fun getCurrentPlaylist(): String? {
        return getMasterDevice()?.getCurrentPlaylist()
    }

    override fun showFrame(name: String, frame: XledFrame) {
        // not implemented yet
    }

    override fun showSequence(
        name: String,
        sequence: XledSequence,
        fps: Int
    ) {
        // not implemented yet
    }


    override fun getMusicEffects(): MusicEffectsResponse? {
        return getMasterDevice()?.getMusicEffects()
    }

    override fun getCurrentMusicEffect(): CurrentMusicEffectResponse? {
        return getMasterDevice()?.getCurrentMusicEffect()
    }

    override fun setCurrentMusicEffect(effectId: String): JsonObject? {
        return getMasterDevice()?.setCurrentMusicEffect(effectId)
    }

    override fun getMusicConfig(): MusicConfig? {
        return getMasterDevice()?.getMusicConfig()
    }

    override fun getLedMusicStats(): LedMusicStatsResponse? {
        return getMasterDevice()?.getLedMusicStats()
    }

    override fun getMusicEnabled(): MusicEnabledResponse? {
        return getMasterDevice()?.getMusicEnabled()
    }

    override fun setMusicEnabled(enabled: Boolean): JsonObject? {
        return getMasterDevice()?.setMusicEnabled(enabled)
    }

    override fun getMusicDriversCurrent(): CurrentMusicDriversResponse? {
        return getMasterDevice()?.getMusicDriversCurrent()
    }

    override fun getMusicDriversSets(): MusicDriversSets? {
        return getMasterDevice()?.getMusicDriversSets()
    }

    override fun getCurrentMusicDriversSet(): CurrentMusicDriverSetResponse? {
        return getMasterDevice()?.getCurrentMusicDriversSet()
    }


    override fun setBrightness(brightness: Float) {
        xLedDevices.flatten().forEach { it.setBrightness(brightness) }
    }

    override fun getSaturation(): Saturation? {
        return getMasterDevice()?.getSaturation()
    }

    override fun setSaturation(saturation: Float) {
        xLedDevices.flatten().forEach { it.setSaturation(saturation) }
    }

    override fun getColor(): Color<*> {
        return getMasterDevice()?.getColor()?: RGBColor(0, 0, 0)
    }

    override fun setColor(color: Color<*>) {
        xLedDevices.flatten().forEach { it.setColor(color) }
    }

    override fun getLedConfig(): LedConfigResponse? {
        return getMasterDevice()?.getLedConfig()
    }


    override fun getTimer(): Timer {
        return getMasterDevice()?.getTimer()?:error(NO_DEVICE)
    }

    override fun setTimer(timeOn: OffsetDateTime, timeOff: OffsetDateTime): Timer {
        return xLedDevices.flatten().firstOrNull()?.setTimer(timeOn, timeOff)?:error(NO_DEVICE)
    }

    override fun setTimer(timer: Timer): Timer {
        return xLedDevices.flatten().firstOrNull()?.setTimer(timer)?:error(NO_DEVICE)
    }

    override fun setTimer(timeOnHour: Int, timeOnMinute: Int, timeOffHour: Int, timeOffMinute: Int): Timer {
        return xLedDevices.flatten().firstOrNull()?.setTimer(timeOnHour, timeOnMinute, timeOffHour, timeOffMinute)?:error(
            NO_DEVICE
        )
    }

    fun rotateRight(): XLedArray {
        val newArray = XLedArray(xLedDevices = xLedDevices, width = rows, height = columns)
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                newArray[y, columns - x - 1] = this[x, y]
            }
        }
        newArray.initialize()

        return newArray
    }

    fun rotateLeft(): XLedArray {
        val newArray = XLedArray(xLedDevices = xLedDevices, width = rows, height = columns)
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                newArray[rows - y - 1, x] = this[x, y]
            }
        }
        newArray.initialize()

        return newArray
    }

    fun rotate180(): XLedArray {
        val newArray = XLedArray(xLedDevices = xLedDevices, width = columns, height = rows)
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                newArray[columns - x - 1, rows - y - 1] = this[x, y]
            }
        }
        newArray.initialize()

        return newArray
    }

    override fun showRealTimeFrame(frame: XledFrame) {
        if (deviceOrigin.isPortrait()) {
            showFramePortrait(frame)
        } else  {
            showFrameLandscape(frame)
        }
    }

    override fun setLedMovieConfig(movieConfig: LedMovieConfigResponse): JsonObject? {
        return getMasterDevice()?.setLedMovieConfig(movieConfig)
    }

    override fun uploadNewMovie(newMovie: NewMovieRequest): NewMovieResponse? {
        return getMasterDevice()?.uploadNewMovie(newMovie)
    }

    override fun uploadNewMovieToListOfMovies(frame: XledFrame): Movie? {
        return getMasterDevice()?.uploadNewMovieToListOfMovies(frame)
    }

    override fun uploadNewMovieToListOfMovies(bytes: ByteArray): Movie? {
        return getMasterDevice()?.uploadNewMovieToListOfMovies(bytes)
    }

    private fun showFramePortrait(frame: XledFrame) {
        val translatedArray = when (deviceOrigin) {
            DeviceOrigin.BOTTOM_RIGHT -> rotate180()
            else -> this
        }
        val firstDevice = translatedArray[0, 0]
        val offsetX = firstDevice.width
        val offsetY = firstDevice.height
        for (x in 0 until columns) {
            for (y in 0 until rows) {
                val xledDevice = translatedArray[x, y]
                val offsetX1 = x * offsetX
                val offsetY1 = y * offsetY
                if (offsetX1 < frame.width && offsetY1 < frame.height) {
                    val subFrame = frame.subFrame(offsetX1, offsetY1, xledDevice.width, xledDevice.height)
                    val translatedFrame = when (deviceOrigin) {
                        DeviceOrigin.BOTTOM_RIGHT -> subFrame.rotate180()
                        else -> subFrame
                    }
                    xledDevice.showRealTimeFrame(translatedFrame)
                }
            }
        }
    }

    private fun showFrameLandscape(frame: XledFrame) {
        val translatedArray = when (deviceOrigin) {
            DeviceOrigin.TOP_RIGHT -> rotateLeft()
            DeviceOrigin.BOTTOM_LEFT -> rotateRight()
            else -> this
        }
        val firstDevice = translatedArray[0, 0]
        val offsetX = firstDevice.height
        val offsetY = firstDevice.width
        for (x in 0 until rows) {
            for (y in 0 until columns) {
                val xledDevice = translatedArray[x, y]
                val subFrame = frame.subFrame(x * offsetX, y * offsetY, xledDevice.height, xledDevice.width)
                val translatedFrame = when (deviceOrigin) {
                    DeviceOrigin.TOP_RIGHT -> subFrame.rotateLeft()
                    DeviceOrigin.BOTTOM_LEFT -> subFrame.rotateRight()
                    else -> subFrame
                }
                xledDevice.showRealTimeFrame(translatedFrame)
            }
        }
    }
}
