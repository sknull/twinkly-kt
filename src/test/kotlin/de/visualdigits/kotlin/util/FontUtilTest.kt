package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

class FontUtilTest {

    private val xledArray = XledArray(listOf(
        XLedDevice("192.168.178.35", deviceOrigin = DeviceOrigin.TOP_LEFT),
        XLedDevice("192.168.178.52", deviceOrigin = DeviceOrigin.TOP_LEFT)
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

        sequence.play(xledArray)
    }

    @Test
    fun testLandscapeText() {
        val xledArrayLandscape = XledArray(
            xLedDevices = listOf(
                XLedDevice("192.168.178.35", deviceOrigin = DeviceOrigin.TOP_RIGHT),
                XLedDevice("192.168.178.52", deviceOrigin = DeviceOrigin.TOP_RIGHT)
            )
        )

        val sequence = XledSequence(
            fontName = "fonts/Only When I Do Fonts Regular.ttf",
            fontSize = 10,
            targetWidth = xledArrayLandscape.width,
            targetHeight = xledArrayLandscape.height,
            frameDelay = 100,
            Triple("Merry ", RGBColor(0, 0, 0), RGBColor(255, 255, 255)),
            Triple("Christmas", RGBColor(0, 0, 0), RGBColor(255, 0, 0)),
            Triple("!", RGBColor(0, 0, 0), RGBColor(255, 255, 255))
        )

        sequence.play(xledArrayLandscape)
    }
}
