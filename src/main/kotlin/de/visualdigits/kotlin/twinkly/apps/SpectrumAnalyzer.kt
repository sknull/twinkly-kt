package de.visualdigits.kotlin.twinkly.apps

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import de.visualdigits.minim.Minim
import de.visualdigits.minim.analysis.BeatDetect
import de.visualdigits.minim.analysis.FFT
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Displays a spectrum on the given xled device.
 *
 * @param colorMeter The color used for the spectrum bands.
 * @param colorMax The color used for the max values of the bands (moving down over time)
 * @param colorMeterBeat The color used for the spectrum bands when on a beat.
 * @param colorMaxBeat The color used for the max values of the bands when on a beat (moving down over time)
 * @param xled The xled device to use.
 */
class SpectrumAnalyzer(
    val colorMeter: Color<*> = RGBColor(0, 255, 0),
    val colorMax: Color<*> = RGBColor(255, 0, 0),
    val colorMeterBeat: Color<*> = RGBColor(255, 255, 255),
    val colorMaxBeat: Color<*> = RGBColor(255, 0, 0),
    val xled: XLed
) {

    fun run() {
        val minim = Minim()
        val player = minim.lineIn
        val beat = BeatDetect()
        val fft = FFT(player.bufferSize(), player.sampleRate())
        val spectrumSize = fft.specSize() / 16
        val stepSize = ceil(spectrumSize.toDouble() / xled.width).toInt()
        val maxAmplitudes = MutableList<Int>(xled.width) { 0 }
        player.disableMonitoring()
        xled.mode(DeviceMode.rt)
        var t = 0
        while(true) {
            fft.forward(player.mix)
            beat.detect(player.mix)
            val meterColor = if (beat.isOnset) colorMeterBeat else colorMeter
            val maxColor = if (beat.isOnset) colorMaxBeat else colorMax
            val frame = XledFrame(xled.width, xled.height)
            var x = 0
            val values = mutableListOf<Float>()
            for (i in 0 until spectrumSize step stepSize) {
                val band = fft.getBand(i)
                values.add(band)
                val amplitude = min((band / 50.0 * xled.height).toInt(), xled.height)
                val max = max(maxAmplitudes[x], amplitude)
                maxAmplitudes[x] = if (max != maxAmplitudes[x]) {
                    max
                } else if (t++ == 10) {
                    t = 0
                    max(0, maxAmplitudes[x] - 1)
                } else {
                    maxAmplitudes[x]
                }
                for (y in xled.height - amplitude until xled.height) {
                    frame[x][y] = meterColor
                }
                x++
            }
            for (x in 0 until xled.width) {
                val y = max(0, xled.height - maxAmplitudes[x] - 1)
                frame[x][y] = maxColor
            }
            xled.showRealTimeFrame(frame)
            Thread.sleep(20)
        }
    }
}
