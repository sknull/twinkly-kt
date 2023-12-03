package de.visualdigits.kotlin.twinkly.apps

import de.visualdigits.kotlin.minim.AudioInputType
import de.visualdigits.kotlin.minim.BeatDetect
import de.visualdigits.kotlin.minim.Minim
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Displays a spectrum on the given xled device.
 *
 * @param color The color used for the spectrum bands.
 * @param colorBeat The color used for the spectrum bands when on a beat.
 * @param xled The xled device to use.
 */
class Oscilloscope(
    private val color: Color<*> = RGBColor(255, 0, 0),
    private val colorBeat: Color<*> = RGBColor(255, 255, 255),
    private val xled: XLed
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
            for ((x, i) in (0 until bufferSize - stepSize step stepSize).withIndex()) {
                val left = (i until i + stepSize).map { j -> player.left[j] + 1.0 }
                val lAverage =(left.sum() / left.size.toDouble() * h / 2.0).roundToInt()
                frame[x][max(h - 1 - lAverage, 0)] = c
            }
            xled.showRealTimeFrame(frame)
            Thread.sleep(20)
        }
    }
}
