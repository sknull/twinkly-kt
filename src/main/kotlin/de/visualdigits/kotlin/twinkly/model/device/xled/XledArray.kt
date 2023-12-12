package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame

class XledArray(
    val xLedDevices: List<XLedDevice>,
    val deviceOrigin: DeviceOrigin = DeviceOrigin.TOP_LEFT
) : XLed {

    override val width: Int
    override val height: Int
    override val bytesPerLed: Int = xLedDevices.firstOrNull()?.bytesPerLed?:0

    init {
        if (deviceOrigin.portrait()) {
            width = xLedDevices.sumOf { it.width }
            height = xLedDevices.maxOfOrNull { it.height } ?: 0
        } else {
            width = xLedDevices.sumOf { it.height }
            height = xLedDevices.maxOfOrNull { it.width } ?: 0
        }
    }

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

    override fun mode(): DeviceMode {
        return xLedDevices.map { it.mode() }.first()
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
            xled.showRealTimeFrame(frame.subFrame(offsetX, 0, xled.width, xled.height))
            offsetX += xled.width
        }
    }
}
