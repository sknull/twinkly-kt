package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.visualization.Oscilloscope
import de.visualdigits.kotlin.twinkly.visualization.SpectrumAnalyzer
import de.visualdigits.kotlin.twinkly.visualization.VUMeter
import de.visualdigits.kotlin.twinkly.visualization.Visualizer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("only for local testing")
class Demos : XledArrayTest() {

    @Test
    fun testOscilloscope() {
        val oscilloscope = Oscilloscope(
            xled = xledArrayLandscapeRight
        )
        oscilloscope.run()
    }

    @Test
    fun testSpectrumAnalyzer() {
        val analyzer = SpectrumAnalyzer(
            xled = xledArrayLandscapeRight
        )
        analyzer.run()
    }

    @Test
    fun testVUMeter() {
        val vuMeter = VUMeter(
            xled = xledArray
        )
        vuMeter.run()
    }

    @Test
    fun testVisualizer() {
        val visualizer = Visualizer(
            xled = xledArray
        )
        visualizer.run()
    }
}
