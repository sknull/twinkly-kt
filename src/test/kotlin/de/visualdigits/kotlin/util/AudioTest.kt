package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.visualization.Oscilloscope
import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import de.visualdigits.kotlin.twinkly.visualization.SpectrumAnalyzer
import de.visualdigits.kotlin.twinkly.visualization.SpectrumQuad
import de.visualdigits.kotlin.twinkly.visualization.VUMeter
import de.visualdigits.kotlin.twinkly.visualization.Visualizer
import org.junit.jupiter.api.Test

class AudioTest {

    private val xledArray = XledArray(listOf(
        XLedDevice("192.168.178.35", deviceOrigin = DeviceOrigin.BOTTOM_LEFT),
        XLedDevice("192.168.178.52", deviceOrigin = DeviceOrigin.BOTTOM_LEFT)
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
        val oscilloscope = Oscilloscope(
            xled = xledArray
        )
        oscilloscope.run()
    }

    @Test
    fun testVisualizer() {
        val visualizer = Visualizer(
            xled = xledArray
        )
        visualizer.run()
    }

    @Test
    fun testVUMeter() {
        val vuMeter = VUMeter(
            xled = xledArray
        )
        vuMeter.run()
    }
}
