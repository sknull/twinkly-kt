package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence

interface XLed {

    val width: Int
    val height: Int
    val bytesPerLed: Int

    fun logout()

    fun powerOn()

    fun powerOff()

    fun getMode(): DeviceMode

    fun setMode(mode: DeviceMode): JsonObject

    fun ledReset()

    fun setBrightness(brightness: Brightness)

    fun setSaturation(saturation: Saturation)

    fun setColor(color: Color<*>)

    fun showRealTimeSequence(frameSequence: XledSequence, loop: Int = 1) {
        val frames = frameSequence
            .filter { it is XledFrame }
            .map { it as XledFrame }

        var loopCount = loop
        while (loopCount == -1 || loopCount > 0) {
            frames.forEach { frame ->
                showRealTimeFrame(frame)
                Thread.sleep(frameSequence.frameDelay)
            }
            if (loopCount != -1) loopCount--
        }
    }

    fun showRealTimeFrame(frame: XledFrame)
}
