package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import de.visualdigits.kotlin.twinkly.visualization.Oscilloscope
import de.visualdigits.kotlin.twinkly.visualization.SpectrumAnalyzer
import de.visualdigits.kotlin.twinkly.visualization.SpectrumQuad
import de.visualdigits.kotlin.twinkly.visualization.VUMeter
import de.visualdigits.kotlin.twinkly.visualization.Visualizer
import org.junit.jupiter.api.Test

class AudioTest {

    private val xledArray = XledArray(
        arrayOf(
            arrayOf(
                XLedDevice("192.168.178.35", 10, 21),
                XLedDevice("192.168.178.58", 10, 21),
            ),
            arrayOf(
                XLedDevice("192.168.178.52", 10, 21),
                XLedDevice("192.168.178.60", 10, 21)
            )
        )
    )

    private val xledArrayLandscapeLeft = XledArray(
        arrayOf(
            arrayOf(
                XLedDevice("192.168.178.35", 10, 21),
                XLedDevice("192.168.178.58", 10, 21),
            ),
            arrayOf(
                XLedDevice("192.168.178.52", 10, 21),
                XLedDevice("192.168.178.60", 10, 21)
            )
        ),
        DeviceOrigin.BOTTOM_LEFT
    )

    private val xledArrayLandscapeRight = XledArray(
        arrayOf(
            arrayOf(
                XLedDevice("192.168.178.35", 10, 21),
                XLedDevice("192.168.178.58", 10, 21),
            ),
            arrayOf(
                XLedDevice("192.168.178.52", 10, 21),
                XLedDevice("192.168.178.60", 10, 21)
            )
        ),
        DeviceOrigin.TOP_RIGHT
    )

    @Test
    fun testSpectrumQuad() {
        val analyzer = SpectrumQuad(
            xled = xledArrayLandscapeLeft
        )
        analyzer.run()
    }
}
