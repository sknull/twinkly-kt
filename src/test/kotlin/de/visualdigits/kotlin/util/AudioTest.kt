package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.apps.SpectrumAnalyzer
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
    fun testKraftwerk() {
        val analyzer = SpectrumAnalyzer(
            colorMeter = RGBWColor(50, 255, 64, 50),
            colorMax = RGBWColor(255, 50, 0, 0),
            colorMeterBeat = RGBWColor(50, 50, 255, 50),
            colorMaxBeat = RGBWColor(0, 255, 255, 0),
            xled = xledArray
        )
        analyzer.run()
    }
}
