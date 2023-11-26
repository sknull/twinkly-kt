package de.visualdigits.kotlin.twinkly.model

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.conway.Conway
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.frame.XledSequence
import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import org.junit.jupiter.api.Test
import java.io.File

class XledArrayTest {

    private val xled1 = XLedDevice("192.168.178.35")
    private val xled2 = XLedDevice("192.168.178.52")
    private val xledArray = XledArray(listOf(xled1, xled2))

    @Test
    fun testMovingStripes() {
        xledArray.mode(DeviceMode.rt)
        val sequence = XledSequence()
        for (y in 0 until 21) {
            val frame = XledFrame(20, 21, 4)
            for (x in 0 until 20) {
                frame[x][y] = RGBColor(255, 255, 255)
            }
            sequence.add(frame)
        }
        xledArray.showRealTimeSequence(sequence, 300)
    }

    @Test
    fun testShowSequence() {
        xledArray.mode(DeviceMode.rt)
        val sequence = XledSequence.fromDirectory(20, 21, 4,  File(ClassLoader.getSystemResource("images/glitter").toURI()))
        while(true) {
            xledArray.showRealTimeSequence(sequence, 100)
        }
    }

    @Test
    fun testColors() {
        xledArray.color(RGBWColor(0, 0, 0, 255))
    }

    @Test
    fun testConwaysGameOfLife() {
        val conway = Conway(
            preset = File(ClassLoader.getSystemResource("conway/conway_gosper.png").toURI()),
            xled = xledArray
        )
        conway.run()
    }
}
