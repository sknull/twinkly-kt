package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.frame.XledSequence
import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

class FontUtilTest {

    private val xledArray = XledArray(listOf(
        XLedDevice("192.168.178.35"),
        XLedDevice("192.168.178.52")
    ))

    @Test
    fun writeBanner() {
        val image = FontUtil.drawText(
            "Hello World! ggg",
            21,
            "fonts/BigSquareDots.ttf",
            RGBColor(255, 0, 0)
        )
        ImageIO.write(image, "png", File("c:/tmp/helloworld.png"))
    }

    @Test
    fun testText() {
        val banner = XledFrame.fromImage(
            FontUtil.drawText(
                "Hello World!",
                18,
                "fonts/5x7-dot-matrix.ttf",
                RGBColor(255, 0, 0)
                )
        )
        val w = banner.width
        val h = banner.height

        xledArray.mode(DeviceMode.rt)

        val sequence = XledSequence()
        for (x in 0 until banner.width - xledArray.width) {
            val frame = banner.subFrame(x, 0, xledArray.width, xledArray.height)
            sequence.add(frame)
        }

        while (true) {
            xledArray.showRealTimeSequence(sequence, 100)
        }
    }
}
