package de.visualdigits.kotlin.twinkly.apps

import de.visualdigits.kotlin.minim.Minim
import de.visualdigits.kotlin.minim.analysis.BeatDetect
import de.visualdigits.kotlin.minim.analysis.DetectMode
import de.visualdigits.kotlin.minim.audio.AudioInputType
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame

/**
 * Displays a spectrum on the given xled device.
 *
 * @param colorMeter The color used for the spectrum bands.
 * @param colorMax The color used for the max values of the bands (moving down over time)
 * @param colorMeterKick The color used for the spectrum bands when on a beat.
 * @param colorMaxKick The color used for the max values of the bands when on a beat (moving down over time)
 * @param xled The xled device to use.
 */
class SpectrumQuad(
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
        val player = minim.getLineIn(AudioInputType.MONO)!!
        val beat = BeatDetect(algorithm = DetectMode.FREQ_ENERGY)
        player.disableMonitoring()
        xled.setMode(DeviceMode.rt)
        while(true) {
            beat.detect(player.mix)

            val kick = beat.isKick()
            val hiHat = beat.isHiHat()
            val snare = beat.isSnare()

            val frame = XledFrame(xled.width, xled.height, RGBWColor(0, 0, 0, 0))
            if (kick) {
                val kickFrame = XledFrame(3, 5, colorMeterKick)
                frame.replaceSubFrame(kickFrame, 6, 5)
            }
            if (hiHat) {
                val hiHatkFrame = XledFrame(3, 5, colorMeterHiHat)
                frame.replaceSubFrame(hiHatkFrame, 10, 5)
            }
            if (snare) {
                val snareFrame = XledFrame(3, 5, colorMeterSnare)
                frame.replaceSubFrame(snareFrame, 14, 5)
            }
            xled.showRealTimeFrame(frame)
            Thread.sleep(5)
        }
    }
}
