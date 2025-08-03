package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.device.xled.response.FirmwareVersionResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.CurrentLedEffectResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedEffectsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedLayout
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.LedMode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.Mode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.SyncMode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicDriverSetResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicDriversResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicEffectResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicDriversSets
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicEffectsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicEnabledResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicStatsResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MusicConfig
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import java.time.OffsetDateTime

private const val NO_DEVICE = "No device"

class XledArray(
    var xLedDevices: Array<Array<XLedDevice>> = arrayOf(),
    val deviceOrigin: DeviceOrigin = DeviceOrigin.TOP_LEFT,
    override var width: Int = 0,
    override var height: Int = 0,
    override val transformation:  ((XledFrame) -> XledFrame)? = null
) : XLed {

    val columns = xLedDevices.size
    val rows = if (xLedDevices.isNotEmpty()) xLedDevices.minOf { column -> column.size } else 0
    override val bytesPerLed: Int = getMasterDevice()?.bytesPerLed?:0

    init {
        initalize()
    }

    private fun initalize() {
        if (xLedDevices.isNotEmpty()) {
            if(deviceOrigin.isPortrait()) {
                width = (0 until rows ).maxOf { y -> (0 until columns).sumOf { x -> xLedDevices[x][y].width } }
                height = (0 until columns ).maxOf { x -> xLedDevices[x].sumOf { row -> row.height } }
            } else {
                width = (0 until columns ).maxOf { x -> xLedDevices[x].sumOf { row -> row.height } }
                height = (0 until rows ).maxOf { y -> (0 until columns).sumOf { x -> xLedDevices[x][y].width } }
            }
        }
    }

    fun isLoggedIn(): Boolean = xLedDevices.all { column -> column.all { device -> device.isLoggedIn() } }

    operator fun set(x: Int, y: Int, device: XLedDevice) {
        xLedDevices[x][y] = device
    }

    operator fun get(x: Int, y: Int): XLedDevice = xLedDevices[x][y]

    override fun logout() {
        xLedDevices.flatten().forEach { it.logout() }
    }

    override fun powerOn() {
        getMasterDevice()?.powerOn()
    }

    override fun powerOff() {
        getMasterDevice()?.powerOff()
    }

    fun getMasterDevice(): XLedDevice? {
        return xLedDevices.flatten().find { d -> d.ledMovieConfig?.sync?.mode == SyncMode.master }
    }

    override fun ledReset() {
        xLedDevices.flatten().forEach { it.ledReset() }
    }

    override fun getMode(): Mode? {
        return getMasterDevice()?.getMode()
    }

    override fun getDeviceMode(): LedMode? {
        return getMasterDevice()?.getDeviceMode()
    }

    override fun setLedMode(mode: LedMode): JsonObject? {
        return getMasterDevice()?.setLedMode(mode)
    }

    fun getLedEffects(): LedEffectsResponse? {
        return getMasterDevice()?.getLedEffects()
    }

    fun getCurrentLedEffect(): CurrentLedEffectResponse? {
        return getMasterDevice()?.getCurrentLedEffect()
    }

    fun setCurrentLedEffect(effectId: String): JsonObject? {
        return getMasterDevice()?.setCurrentLedEffect(effectId)
    }


    fun getMusicEffects(): MusicEffectsResponse? {
        return getMasterDevice()?.getMusicEffects()
    }

    fun getCurrentMusicEffect(): CurrentMusicEffectResponse? {
        return getMasterDevice()?.getCurrentMusicEffect()
    }

    fun setCurrentMusicEffect(effectId: String): JsonObject? {
        return getMasterDevice()?.setCurrentMusicEffect(effectId)
    }

    fun getMusicConfig(): MusicConfig? {
        return getMasterDevice()?.getMusicConfig()
    }

    fun getMusicStats(): MusicStatsResponse? {
        return getMasterDevice()?.getMusicStats()
    }

    fun getMusicEnabled(): MusicEnabledResponse? {
        return getMasterDevice()?.getMusicEnabled()
    }

    fun setMusicEnabled(enabled: Boolean): JsonObject? {
        return getMasterDevice()?.setMusicEnabled(enabled)
    }

    fun getMusicDriversCurrent(): CurrentMusicDriversResponse? {
        return getMasterDevice()?.getMusicDriversCurrent()
    }

    fun getMusicDriversSets(): MusicDriversSets? {
        return getMasterDevice()?.getMusicDriversSets()
    }

    fun getCurrentMusicDriversSet(): CurrentMusicDriverSetResponse? {
        return getMasterDevice()?.getCurrentMusicDriversSet()
    }


    fun getDeviceInfoResponse(): DeviceInfo? {
        return getMasterDevice()?.getDeviceInfoResponse()
    }

    fun getFirmwareVersionResponse(): FirmwareVersionResponse? {
        return getMasterDevice()?.getFirmwareVersionResponse()
    }

    fun determineDeviceGeneration(): Int {
        return getMasterDevice()?.determineDeviceGeneration()?:0
    }

    override fun getLedLayoutResponse(): LedLayout? {
        return getMasterDevice()?.getLedLayoutResponse()
    }


    override fun setBrightness(brightness: Float) {
        xLedDevices.flatten().forEach { it.setBrightness(brightness) }
    }

    override fun setSaturation(saturation: Float) {
        xLedDevices.flatten().forEach { it.setSaturation(saturation) }
    }

    override fun setColor(color: Color<*>) {
        xLedDevices.flatten().forEach { it.setColor(color) }
    }


    override fun getTimer(): Timer {
        return getMasterDevice()?.getTimer()?:error(NO_DEVICE)
    }

    override fun setTimer(timeOn: OffsetDateTime, timeOff: OffsetDateTime): Timer {
        return getMasterDevice()?.setTimer(timeOn, timeOff)?:error(NO_DEVICE)
    }

    override fun setTimer(timer: Timer): Timer {
        return getMasterDevice()?.setTimer(timer)?:error(NO_DEVICE)
    }

    override fun setTimer(timeOnHour: Int, timeOnMinute: Int, timeOffHour: Int, timeOffMinute: Int): Timer {
        return getMasterDevice()?.setTimer(timeOnHour, timeOnMinute, timeOffHour, timeOffMinute)?:error(
            NO_DEVICE
        )
    }

    fun rotateRight(): XledArray {
        val newArray = XledArray(xLedDevices = xLedDevices, width = rows, height = columns)
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                newArray[y, columns - x - 1] = this[x, y]
            }
        }
        newArray.initalize()

        return newArray
    }

    fun rotateLeft(): XledArray {
        val newArray = XledArray(xLedDevices = xLedDevices, width = rows, height = columns)
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                newArray[rows - y - 1, x] = this[x, y]
            }
        }
        newArray.initalize()

        return newArray
    }

    fun rotate180(): XledArray {
        val newArray = XledArray(xLedDevices = xLedDevices, width = columns, height = rows)
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                newArray[columns - x - 1, rows - y - 1] = this[x, y]
            }
        }
        newArray.initalize()

        return newArray
    }

    override fun showRealTimeFrame(frame: XledFrame) {
        if (deviceOrigin.isPortrait()) {
            showFramePortrait(frame)
        } else  {
            showFrameLandscape(frame)
        }
    }

    private fun showFramePortrait(frame: XledFrame) {
        val translatedArray = when (deviceOrigin) {
            DeviceOrigin.BOTTOM_RIGHT -> rotate180()
            else -> this
        }
        val firstDevice = translatedArray.xLedDevices[0][0]
        val offsetX = firstDevice.width
        val offsetY = firstDevice.height
        for (x in 0 until columns) {
            for (y in 0 until rows) {
                val xledDevice = translatedArray.xLedDevices[x][y]
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
        val firstDevice = translatedArray.xLedDevices[0][0]
        val offsetX = firstDevice.height
        val offsetY = firstDevice.width
        for (x in 0 until rows) {
            for (y in 0 until columns) {
                val xledDevice = translatedArray.xLedDevices[x][y]
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
