package de.visualdigits.kotlin.twinkly.apps

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import de.visualdigits.minim.AudioInputType
import de.visualdigits.minim.Minim
import de.visualdigits.minim.analysis.BeatDetect
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Displays a spectrum on the given xled device.
 *
 * @param colorMeter The color used for the spectrum bands.
 * @param colorMax The color used for the max values of the bands (moving down over time)
 * @param colorMeterBeat The color used for the spectrum bands when on a beat.
 * @param colorMaxBeat The color used for the max values of the bands when on a beat (moving down over time)
 * @param xled The xled device to use.
 */
class Oscilloscope(
    val color: Color<*> = RGBColor(255, 0, 0),
    val colorBeat: Color<*> = RGBColor(255, 255, 255),
    val xled: XLed
) {

    fun run() {
        val minim = Minim()
        val player = minim.getLineIn(AudioInputType.MONO)!!
        val beat = BeatDetect()
        player.disableMonitoring()
        xled.mode(DeviceMode.rt)
        val bufferSize = player.bufferSize()
        val stepSize = ceil(bufferSize.toDouble() / xled.width).toInt()

        val w = xled.width
        val h = xled.height

        while(true) {
            beat.detect(player.mix)
            val c = if (beat.isOnset) colorBeat else color
            val frame = XledFrame(w, h)
            var x = 0
            for (i in 0 until bufferSize - stepSize step stepSize) {
                val left = (i until i + stepSize).map { j -> player.left[j] + 1.0 }
                val lAverage =(left.sum() / left.size.toDouble() * h / 2.0).roundToInt()
                frame[x][max(h - 1 - lAverage, 0)] = c
                x++
            }
            xled.showRealTimeFrame(frame)
            Thread.sleep(20)
        }
    }
}
