package de.visualdigits.kotlin.twinkly.apps

import de.visualdigits.kotlin.minim.Minim
import de.visualdigits.kotlin.minim.analysis.BeatDetect
import de.visualdigits.kotlin.minim.analysis.DetectMode
import de.visualdigits.kotlin.minim.analysis.FFT
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Displays a spectrum on the given xled device.
 *
 * @param colorMeter The color used for the spectrum bands.
 * @param colorMax The color used for the max values of the bands (moving down over time)
 * @param colorMeterKick The color used for the spectrum bands when on a beat.
 * @param colorMaxKick The color used for the max values of the bands when on a beat (moving down over time)
 * @param xled The xled device to use.
 */
class SpectrumAnalyzer(
    private val colorMeter: Color<*> = RGBWColor(0, 255, 0, 0),
    private val colorMax: Color<*> = RGBWColor(255, 0, 0, 0),
    private val colorMeterKick: Color<*> = RGBWColor(255, 0, 0, 0),
    private val colorMaxKick: Color<*> = RGBWColor(255, 0, 0, 128),
    private val colorMeterHiHat: Color<*> = RGBWColor(255, 255, 0, 0),
    private val colorMaxHiHat: Color<*> = RGBWColor(255, 255, 0, 128),
    private val colorMeterSnare: Color<*> = RGBWColor(0, 0, 255, 0),
    private val colorMaxSnare: Color<*> = RGBWColor(0, 0, 255, 128),
    private val xled: de.visualdigits.kotlin.twinkly.model.device.xled.XLed
) {

    fun run() {
        val minim = Minim()
        val player = minim.getLineIn()!!
        val beat = BeatDetect(algorithm = DetectMode.FREQ_ENERGY)
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
            val kick = beat.isKick()
            val hiHat = beat.isHiHat()
            val snare = beat.isSnare()
            val meterColor = if (kick) {
                colorMeterKick
            }
            else if (hiHat) {
                colorMeterHiHat
            }
            else if (snare) {
                colorMeterSnare
            }
            else {
                colorMeter
            }
            val maxColor = if (kick) {
                colorMaxKick
            } else if (hiHat) {
                    colorMaxHiHat
            } else if (snare) {
                    colorMaxSnare
            } else {
                colorMax
            }
            val frame = XledFrame(xled.width, xled.height)
            val values = mutableListOf<Float>()
            for ((x, i) in (0 until spectrumSize step stepSize).withIndex()) {
                val band = fft.getBand(i)
                values.add(band)
                val amplitude = min((band / 50.0 * xled.height).toInt(), xled.height)
                val max = max(maxAmplitudes[x], amplitude)
                maxAmplitudes[x] = if (max != maxAmplitudes[x]) {
                    max
                } else if (t++ == 3) {
                    t = 0
                    max(0, maxAmplitudes[x] - 1)
                } else {
                    maxAmplitudes[x]
                }
                for (y in xled.height - amplitude until xled.height) {
                    frame[x][y] = meterColor
                }
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
