package de.visualdigits.kotlin.twinkly.model.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.frame.XledSequence
import de.visualdigits.kotlin.twinkly.model.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import org.slf4j.LoggerFactory

interface XLed {

    val width: Int
    val height: Int
    val bytesPerLed: Int

    fun logout()

    fun powerOn()

    fun powerOff()

    fun mode(): DeviceMode

    fun mode(mode: DeviceMode): JsonObject

    fun ledReset()

    fun brightness(brightness: Brightness)

    fun saturation(saturation: Saturation)

    fun color(color: Color<*>)

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
