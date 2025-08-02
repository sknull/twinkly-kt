package de.visualdigits.kotlin.twinkly.visualization

import de.visualdigits.kotlin.minim.Minim
import de.visualdigits.kotlin.minim.audio.AudioInputType
import de.visualdigits.kotlin.minim.fft.BeatDetect
import de.visualdigits.kotlin.minim.fft.DetectMode
import de.visualdigits.kotlin.minim.fft.FFT
import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.LedMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random


/**
 * Displays a spectrum on the given xled device.
 *
 * @param colorMeter The color used for the spectrum bands.
 * @param colorMax The color used for the max values of the bands (moving down over time)
 * @param colorMeterKick The color used for the spectrum bands when on a beat.
 * @param colorMaxKick The color used for the max values of the bands when on a beat (moving down over time)
 * @param xled The xled device to use.
 */
class Visualizer(
    private val xled: XLed
) {

    fun run() {
        xled.setLedMode(LedMode.rt)

        val centerX = xled.width / 2
        val centerY = xled.height / 2
        val numberOfPoints = 20
        val rMin = 3

        Random(System.currentTimeMillis())

        val minim = Minim()

        val player = minim.getLineIn(AudioInputType.MONO)!!
        player.disableMonitoring()

        BeatDetect(algorithm = DetectMode.FREQ_ENERGY)

        val fft = FFT(player.bufferSize(), player.sampleRate())
        val spectrumSize = fft.specSize() / 8


        var frame = XledFrame(xled.width, xled.height, RGBColor(0, 0 , 0))
        XledFrame(xled.width, xled.height, RGBColor(0, 0 , 0))

        var f = 1.0
        while(true) {
            fft.forward(player.mix)
            val st = spectrumSize / 16
            val band = (0 until spectrumSize step st).map { b ->
                (0 until st).map { fft.getBand(b + it).toDouble() / 255.0 }.sum() / st
            }

            var c = RGBColor(0, 0, 0)
            c = c.fade(RGBColor(255, 0, 0), band[0], BlendMode.AVERAGE)
            c = c.fade(RGBColor(0, 255, 0), band[1], BlendMode.AVERAGE)
            c = c.fade(RGBColor(0, 0, 255), band[2], BlendMode.AVERAGE)

            var a = 0.0
            val angle = 2 * PI / numberOfPoints
            val s = player.bufferSize() / numberOfPoints
            for (i in 0 until player.bufferSize() - s step s ) {
                val vu = player.mix[i]
                val rx = rMin + (xled.width - rMin) * vu
                val ry = rMin + (xled.height - rMin) * vu
                val x = (centerX + cos(a) * rx).roundToInt()
                val y = (centerY + sin(a) * ry).roundToInt()

                frame[max(0, min(xled.width - 1, x)), max(0, min(xled.height - 1, y))] = c
                a += angle
            }

            xled.showRealTimeFrame(frame)
            f -= 0.001
            if (f <= 0.0) {
                f = 1.0
            }
            for (y in 0 until xled.height) {
                for (x in 0 until xled.width) {
                    val color = frame[x, y] as RGBColor
                    frame[x, y] = RGBColor((color.red * f).roundToInt(), (color.green * f).roundToInt(), (color.blue * f).roundToInt())
                }
            }
            Thread.sleep(10)
        }
    }
}
