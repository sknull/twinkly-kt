package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import java.lang.IllegalStateException
import java.time.OffsetDateTime

class XledArray(
    val xLedDevices: List<XLedDevice>
) : XLed {

    override val width: Int = xLedDevices.sumOf { it.width }
    override val height: Int = xLedDevices.maxOfOrNull { it.height } ?: 0
    override val bytesPerLed: Int = xLedDevices.firstOrNull()?.bytesPerLed?:0

    override fun logout() {
        xLedDevices.forEach { it.logout() }
    }

    override fun powerOn() {
        xLedDevices.forEach { it.powerOn() }
    }

    override fun powerOff() {
        xLedDevices.forEach { it.powerOff() }
    }

    override fun ledReset() {
        xLedDevices.forEach { it.ledReset() }
    }

    override fun getMode(): DeviceMode {
        return xLedDevices.map { it.getMode() }.first()
    }

    override fun setMode(mode: DeviceMode): JsonObject {
        return xLedDevices.map { it.setMode(mode) }.first()
    }

    override fun setBrightness(brightness: Brightness) {
        xLedDevices.forEach { it.setBrightness(brightness) }
    }

    override fun setSaturation(saturation: Saturation) {
        xLedDevices.forEach { it.setSaturation(saturation) }
    }

    override fun setColor(color: Color<*>) {
        xLedDevices.forEach { it.setColor(color) }
    }

    override fun getTimer(): Timer {
        return xLedDevices.firstOrNull()?.getTimer()?:throw IllegalStateException("No device")
    }

    override fun setTimer(timeOn: OffsetDateTime, timeOff: OffsetDateTime): Timer {
        return xLedDevices.firstOrNull()?.setTimer(timeOn, timeOff)?:throw IllegalStateException("No device")
    }

    override fun setTimer(timer: Timer): Timer {
        return xLedDevices.firstOrNull()?.setTimer(timer)?:throw IllegalStateException("No device")
    }

    override fun setTimer(timeOnHour: Int, timeOnMinute: Int, timeOffHour: Int, timeOffMinute: Int): Timer {
        return xLedDevices.firstOrNull()?.setTimer(timeOnHour, timeOnMinute, timeOffHour, timeOffMinute)?:throw IllegalStateException("No device")
    }

    override fun showRealTimeFrame(frame: XledFrame) {
        var offsetX = 0
        xLedDevices.forEach { xled ->
            xled.showRealTimeFrame(frame.subFrame(offsetX, 0, xled.width, xled.height))
            offsetX += xled.width
        }
    }
}
