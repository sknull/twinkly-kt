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
            text = "Hello World! ggg",
            fontName = "fonts/BigSquareDots.ttf",
            fontSize = 21,
            backgroundColor = RGBColor(0, 0, 0),
            textColor = RGBColor(255, 0, 0)
        )
        ImageIO.write(image, "png", File("c:/tmp/helloworld.png"))
    }

    @Test
    fun testText() {
        val sequence = XledSequence(
            fontName = "fonts/Only When I Do Fonts Regular.ttf",
            fontSize = 20,
            targetWidth = xledArray.width,
            targetHeight = xledArray.height,
            frameDelay = 100,
            Triple("Merry ", RGBColor(0, 0, 0), RGBColor(255, 255, 255)),
            Triple("Christmas", RGBColor(0, 0, 0), RGBColor(255, 0, 0)),
            Triple("!", RGBColor(0, 0, 0), RGBColor(255, 255, 255))
        )

        xledArray.mode(DeviceMode.rt)
        sequence.play(xledArray)
    }
}
