package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import java.time.OffsetDateTime

interface XLed {

    var width: Int
    var height: Int
    val bytesPerLed: Int

    fun logout()

    fun powerOn()

    fun powerOff()

    fun getMode(): DeviceMode?

    fun setMode(mode: DeviceMode): JsonObject?

    fun ledReset()

    fun setBrightness(brightness: Float)

    fun setSaturation(saturation: Float)

    fun setColor(color: Color<*>)

    /**
     * Returns the current timer of the device.
     */
    fun getTimer(): Timer?

    fun setTimer(timeOn: OffsetDateTime, timeOff: OffsetDateTime): Timer?

    /**
     * Sets the timer of the device to the given hours and minutes.
     * Hour and minute are assumed to be in the current time zone of the machine and will be converted to UTC time zone
     * as xled device to be in zulu time.
     */
    fun setTimer(timeOnHour: Int, timeOnMinute: Int, timeOffHour: Int, timeOffMinute: Int): Timer?

    fun setTimer(timer: Timer): Timer?

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
