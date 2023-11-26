package de.visualdigits.kotlin.util

import ddf.minim.Minim
import ddf.minim.analysis.FFT
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import org.junit.jupiter.api.Test
import java.io.File
import java.io.InputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class AudioTest {

    private val xledArray = XledArray(listOf(
        XLedDevice("192.168.178.35"),
        XLedDevice("192.168.178.52")
    ))

    @Test
    fun analyze() {
        val minim = Minim(this)
        val player = minim.loadFile("M:/Electronic/Kraftwerk/Der Katalog/2003_Tour De France/07_Aero Dynamik.mp3", 256)
        val fft = FFT(player.bufferSize(), player.sampleRate())
        val spectrumSize = fft.specSize()
        val stepSize = ceil(spectrumSize.toDouble() / xledArray.width).toInt()
        val maxAmplitudes = MutableList<Int>(xledArray.width) { 0 }
        player.play()
        xledArray.mode(DeviceMode.rt)
        var t = 0
        while(true) {
            fft.forward(player.mix)
            val frame = XledFrame(xledArray.width, xledArray.height)
            var x = 0
            val values = mutableListOf<Float>()
            for (i in 0 until spectrumSize step stepSize) {
                val band = fft.getBand(i)
                values.add(band)
                val amplitude = min((band / 50.0 * xledArray.height).toInt(), xledArray.height)
                val max = max(maxAmplitudes[x], amplitude)
                maxAmplitudes[x] = if (max != maxAmplitudes[x]) {
                    max
                } else if (t++ == 100) {
                    t = 0
                    max(0, maxAmplitudes[x] - 1)
                } else {
                    maxAmplitudes[x]
                }
                for (y in xledArray.height - amplitude until xledArray.height) {
                    frame[x][y] = RGBColor(0, 255, 0)
                }
                x++
            }
            for (x in 0 until xledArray.width) {
                val y = max(0, xledArray.height - maxAmplitudes[x] - 1)
                frame[x][y] = RGBColor(255, 0, 0)
            }
            xledArray.showRealTimeFrame(frame)
            Thread.sleep(10)
        }
    }

    fun sketchPath(foo: String): String {
        return ""
    }

    fun createInput(fileName: String): InputStream? {
        return File(fileName).inputStream()
    }
}
