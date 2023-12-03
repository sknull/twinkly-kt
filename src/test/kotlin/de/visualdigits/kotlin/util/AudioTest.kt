package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.apps.Oscilloscope
import de.visualdigits.kotlin.twinkly.apps.SpectrumAnalyzer
import de.visualdigits.kotlin.twinkly.apps.SpectrumQuad
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.xled.XledArray
import org.junit.jupiter.api.Test

class AudioTest {

    private val xledArray = XledArray(listOf(
        XLedDevice("192.168.178.35"),
        XLedDevice("192.168.178.52")
    ))

    @Test
    fun testSpectrumAnalyzer() {
        val analyzer = SpectrumAnalyzer(
            xled = xledArray
        )
        analyzer.run()
    }

    @Test
    fun testSpectrumQuad() {
        val analyzer = SpectrumQuad(
            xled = xledArray
        )
        analyzer.run()
    }

    @Test
    fun testOscilloscope() {
        val analyzer = Oscilloscope(
            xled = xledArray
        )
        analyzer.run()
    }
}
