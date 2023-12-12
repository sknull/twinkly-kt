package de.visualdigits.kotlin.twinkly.apps

import de.visualdigits.kotlin.minim.Minim
import de.visualdigits.kotlin.minim.analysis.BeatDetect
import de.visualdigits.kotlin.minim.analysis.DetectMode
import de.visualdigits.kotlin.minim.analysis.FFT
import de.visualdigits.kotlin.minim.audio.AudioInputType
import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import org.apache.coyote.http11.Constants
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
    private val xled: de.visualdigits.kotlin.twinkly.model.device.xled.XLed
) {

    fun run() {
        val random = Random(System.currentTimeMillis())
        val minim = Minim()
        val player = minim.getLineIn(AudioInputType.MONO)!!
        val beat = BeatDetect(algorithm = DetectMode.FREQ_ENERGY)
        val fft = FFT(player.bufferSize(), player.sampleRate())
        player.disableMonitoring()
        xled.mode(DeviceMode.rt)

        val color = RGBColor(random.nextInt(0, 256), random.nextInt(0, 256), random.nextInt(0, 256))
        val black = RGBColor(0, 0, 0)

        var frame = XledFrame(xled.width, xled.height)
        val frameBlack = XledFrame(xled.width, xled.height)

        var f = 0.0
        var f2 = 0.0
        var c: RGBColor = RGBColor(0, 0, 0)
        while(true) {
            fft.forward(player.mix)
            beat.detect(player.mix)
            val kick = beat.isKick()
            val snare = beat.isSnare()
            val hiHat = beat.isHiHat()
            if (kick) {
                c = c.fade(RGBColor(255, 0, 0), f, BlendMode.AVERAGE)
            }

            if (snare) {
                c = c.fade(RGBColor(0, 255, 0), f, BlendMode.AVERAGE)
            }

            if (hiHat) {
                c = c.fade(RGBColor(0, 0, 255), f, BlendMode.AVERAGE)
            }

            var a = 0.0
            val angle = 2 * PI / 200
            val s = player.bufferSize() / 200
            for (i in 0 until player.bufferSize() - s step s ) {
                val x = (10 + cos(a) * (4 * player.mix[i] + 6)).roundToInt()
                val y = (10 + sin(a) * (4 * player.mix[i] + 6)).roundToInt()
//                val x2 = (10 + cos(Constants.a + angle) * (4 * player.mix[i + s] + 6)).roundToInt()
//                val y2 = (10 + sin(Constants.a + angle) * (4 * player.mix[i + s] + 6)).roundToInt()

                frame[max(0, min(xled.width - 1, x))][max(0, min(xled.height - 1, y))] = c.clone()
//                frame.drawLine(x, y, x2, y2, c)
//println("$x/$y - $x2/$y2")
                a += angle
            }

            xled.showRealTimeFrame(frame)
            f += 0.1
            if (f > 1.0) {
                f = 0.0
            }
            f2 += 0.001
            if (f2 > 1.0) {
                f2 = 0.0
            }
            frame = frame.fade(frameBlack, f2)
            Thread.sleep(10)
        }
    }
}
