package de.visualdigits.kotlin.twinkly.visualization

import de.visualdigits.kotlin.minim.Minim
import de.visualdigits.kotlin.minim.audio.AudioInputType
import de.visualdigits.kotlin.minim.fft.BeatDetect
import de.visualdigits.kotlin.minim.fft.DetectMode
import de.visualdigits.kotlin.minim.fft.FFT
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Displays a spectrum on the given xled device.
 *
 * @param colorMeter The color used for the spectrum bands.
 * @param colorMax The color used for the max values of the bands (moving down over time)
 * @param xled The xled device to use.
 */
class SpectrumAnalyzer(
    private val colorMeter: Color<*> = RGBColor(0, 255, 64, 0),
    private val colorMid: Color<*> = RGBColor(255, 255, 0, 0),
    private val colorMax: Color<*> = RGBColor(255, 64, 32, 0),
    private val xled: XLed
) {

    fun run() {
        xled.setMode(DeviceMode.rt)

        val minim = Minim()

        val player = minim.getLineIn(AudioInputType.MONO)!!
        player.disableMonitoring()

        val fft = FFT(player.bufferSize(), player.sampleRate())
        val offsetX = 4
        fft.linAverages((xled.width.toDouble() / offsetX).roundToInt())
        val beat = BeatDetect(DetectMode.FREQ_ENERGY)

        val spectrumSize = fft.specSize() / 1
        val s = (spectrumSize.toDouble() / xled.width).roundToInt()
        val maxAmplitudes = MutableList(xled.width) { 0 }
        var t = 0
        while(true) {
            val buffer = player.mix.toArray().map { it.toDouble() }.toDoubleArray()
            fft.forward(buffer)
            beat.detect(buffer)
            val color = colorMeter//if (beat.isKick()) colorMax else colorMeter
            val frame = XledFrame(xled.width, xled.height)

            var b = 0
//println()
            for (x in 0 until xled.width step offsetX) {
                val db = (fft.getAvg(b++)).roundToInt()
//print("$db ")
                val vy = max(0, min(xled.height - 1, xled.height - db))

                val frameMeter = XledFrame(2, max(0, db - 1), colorMeter)
                frame.replaceSubFrame(frameMeter, x, vy + 1)
//                val frameMid = XledFrame(2, 2, colorMid)
//                frame.replaceSubFrame(frameMid, x, vy + 1)
                val frameMax = XledFrame(2, 1, colorMax)
                frame.replaceSubFrame(frameMax, x, vy)

//                for (y in min(xled.height - 1, vy) until xled.height) {
//                    frame[x, y] = color
//                    frame[x + 1, y] = color
//                }
//                for (y in min(xled.height - 1, vy + 3) until xled.height) {
//                    frame[x][y] = colorMeter
//                    frame[x + 1][y] = colorMeter
//                }
//                for (y in min(xled.height - 1, vy + 1) until min(xled.height - 1, vy + 3)) {
//                    frame[x][y] = colorMid
//                    frame[x + 1][y] = colorMid
//                }
//                frame[x][vy] = colorMax
//                frame[x + 1][vy] = colorMax
            }

            xled.showRealTimeFrame(frame)
            Thread.sleep(10)
        }
    }
}
