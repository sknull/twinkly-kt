package de.visualdigits.kotlin.twinkly.model.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.frame.XledSequence
import de.visualdigits.kotlin.twinkly.model.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode

class XledArray(
    val xLedDevices: List<XLedDevice>
) : XLed {

    override val columns: Int = xLedDevices.sumOf { it.columns }
    override val rows: Int = xLedDevices.firstOrNull()?.rows?:0
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

    override fun mode(mode: DeviceMode): JsonObject {
        return xLedDevices.map { it.mode(mode) }.first()
    }

    override fun brightness(brightness: Brightness) {
        xLedDevices.forEach { it.brightness(brightness) }
    }

    override fun saturation(saturation: Saturation) {
        xLedDevices.forEach { it.saturation(saturation) }
    }

    override fun color(color: Color<*>) {
        xLedDevices.forEach { it.color(color) }
    }

    override fun showRealTimeFrame(frame: XledFrame) {
        var offsetX = 0
        xLedDevices.forEach { xled ->
            xled.showRealTimeFrame(frame.subFrame(offsetX, 0))
            offsetX += xled.columns
        }
    }
}
