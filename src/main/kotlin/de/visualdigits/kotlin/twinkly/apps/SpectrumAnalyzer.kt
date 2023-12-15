package de.visualdigits.kotlin.twinkly.apps

import de.visualdigits.kotlin.minim.Minim
import de.visualdigits.kotlin.minim.analysis.BeatDetect
import de.visualdigits.kotlin.minim.analysis.DetectMode
import de.visualdigits.kotlin.minim.analysis.FFT
import de.visualdigits.kotlin.minim.audio.AudioInputType
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

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
    private val xled: de.visualdigits.kotlin.twinkly.model.device.xled.XLed
) {

    fun run() {
        xled.mode(DeviceMode.rt)

        val minim = Minim()

        val player = minim.getLineIn(AudioInputType.MONO)!!
        player.disableMonitoring()

        val fft = FFT(player.bufferSize(), player.sampleRate())
        val offsetX = 3
        fft.linAverages(xled.width / offsetX)
        val beat = BeatDetect(DetectMode.FREQ_ENERGY)

        val spectrumSize = fft.specSize() / 2
        val s = (spectrumSize.toDouble() / xled.width).roundToInt()
        val maxAmplitudes = MutableList(xled.width) { 0 }
        var t = 0
        while(true) {
            val buffer = player.mix
            fft.forward(buffer)
            beat.detect(buffer)
            val color = colorMeter//if (beat.isKick()) colorMax else colorMeter
            val frame = XledFrame(xled.width, xled.height)

            var b = 0
            for (x in 0 until xled.width step offsetX) {
                val vu = (fft.getAvg(b++) * xled.height).roundToInt()
                val vy = max(0, xled.height - 1 - vu)

                for (y in min(xled.height - 1, vy) until xled.height) {
                    frame[x][y] = color
                    frame[x + 1][y] = color
                }
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
