package de.visualdigits.kotlin.twinkly.visualization

import de.visualdigits.kotlin.minim.Minim
import de.visualdigits.kotlin.minim.fft.BeatDetect
import de.visualdigits.kotlin.minim.fft.DetectMode
import de.visualdigits.kotlin.minim.fft.FFT
import de.visualdigits.kotlin.minim.audio.AudioInputType
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
 * @param colorMeterKick The color used for the spectrum bands when on a beat.
 * @param colorMaxKick The color used for the max values of the bands when on a beat (moving down over time)
 * @param xled The xled device to use.
 */
class VUMeter(
    private val xled: XLed
) {

    fun run() {
        val minim = Minim()
        val player = minim.getLineIn(AudioInputType.STEREO)!!
        player.disableMonitoring()

        val beat = BeatDetect(algorithm = DetectMode.SOUND_ENERGY)

        val fft = FFT(player.bufferSize(), player.sampleRate())
        xled.setMode(DeviceMode.rt)

        val canvas = XledFrame(xled.width, xled.height)

        while(true) {

            fft.forward(player.left)
            drawChannel(fft, canvas, 1, 8)

            fft.forward(player.right)
            drawChannel(fft, canvas, 11, 8)

//            println("#### l:$vuLeft  r:$vuRight")
            xled.showRealTimeFrame(canvas)

            Thread.sleep(10)
        }
    }

    private fun drawChannel(
        fft: FFT,
        canvas: XledFrame,
        offsetX: Int,
        width: Int
    ) {
        val vu = ((0 until fft.specSize()).map { fft.getBand(it).toDouble() / 2.0 }.sum() / fft.specSize() * xled.height).roundToInt()
        val frameGreen = XledFrame(width, max(0, vu - 3), RGBColor(0, 255, 0))
        val frameYellow = XledFrame(width, min(2, vu), RGBColor(255, 255, 0))
        val frameRed = XledFrame(width, 1, RGBColor(255, 0, 0))
        val frameBlack = XledFrame(width, max(0, xled.height - vu), RGBColor(0, 0, 0))
        canvas.replaceSubFrame(frameGreen, offsetX, max(0, xled.height - vu + 3))
        canvas.replaceSubFrame(frameYellow, offsetX, max(0, xled.height - vu + 1))
        canvas.replaceSubFrame(frameRed, offsetX, max(0, xled.height - vu))
        canvas.replaceSubFrame(frameBlack, offsetX, 0)
    }
}
