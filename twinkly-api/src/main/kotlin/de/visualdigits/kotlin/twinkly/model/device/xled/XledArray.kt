package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import java.time.OffsetDateTime

class XledArray(
    var xLedDevices: Array<Array<XLedDevice>> = arrayOf(),
    val deviceOrigin: DeviceOrigin = DeviceOrigin.TOP_LEFT,
    override var width: Int = 0,
    override var height: Int = 0
) : XLed {

    val columns = xLedDevices.size
    val rows = if (xLedDevices.isNotEmpty()) xLedDevices.minOf { column -> column.size } else 0
    override val bytesPerLed: Int = xLedDevices.flatten().firstOrNull()?.bytesPerLed?:0

    constructor(width: Int, height: Int): this(
        xLedDevices = Array(width) { Array(height) { XLedDevice() } },
        width = width,
        height = height
    )

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
        xLedDevices.flatten().forEach { it.powerOn() }
    }

    override fun powerOff() {
        xLedDevices.flatten().forEach { it.powerOff() }
    }

    override fun ledReset() {
        xLedDevices.flatten().forEach { it.ledReset() }
    }

    override fun getMode(): DeviceMode? {
        return xLedDevices.flatten().firstOrNull()?.getMode()
    }

    override fun setMode(mode: DeviceMode): JsonObject? {
        return xLedDevices.flatten().map { it.setMode(mode) }.firstOrNull()
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
        return xLedDevices.flatten().firstOrNull()?.getTimer()?:throw IllegalStateException("No device")
    }

    override fun setTimer(timeOn: OffsetDateTime, timeOff: OffsetDateTime): Timer {
        return xLedDevices.flatten().firstOrNull()?.setTimer(timeOn, timeOff)?:throw IllegalStateException("No device")
    }

    override fun setTimer(timer: Timer): Timer {
        return xLedDevices.flatten().firstOrNull()?.setTimer(timer)?:throw IllegalStateException("No device")
    }

    override fun setTimer(timeOnHour: Int, timeOnMinute: Int, timeOffHour: Int, timeOffMinute: Int): Timer {
        return xLedDevices.flatten().firstOrNull()?.setTimer(timeOnHour, timeOnMinute, timeOffHour, timeOffMinute)?:throw IllegalStateException("No device")
    }

    fun rotateRight(): XledArray {
        val newArray = XledArray(rows, columns)
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                newArray[y, columns - x - 1] = this[x, y]
            }
        }
        newArray.initalize()

        return newArray
    }

    fun rotateLeft(): XledArray {
        val newArray = XledArray(rows, columns)
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                newArray[rows - y - 1, x] = this[x, y]
            }
        }
        newArray.initalize()

        return newArray
    }

    fun rotate180(): XledArray {
        val newArray = XledArray(columns, rows)
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
        } else  {
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
}
