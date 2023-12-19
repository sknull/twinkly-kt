package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

class FontUtilTest {

    private val xledArray = XledArray(listOf(
        XLedDevice.getInstance("192.168.178.35", deviceOrigin = DeviceOrigin.TOP_LEFT),
        XLedDevice.getInstance("192.168.178.52", deviceOrigin = DeviceOrigin.TOP_LEFT)
    ))

    val xledArrayLandscape = XledArray(
        xLedDevices = listOf(
            XLedDevice.getInstance("192.168.178.35", deviceOrigin = DeviceOrigin.BOTTOM_LEFT),
            XLedDevice.getInstance("192.168.178.52", deviceOrigin = DeviceOrigin.BOTTOM_LEFT)
        )
    )
    @Test
    fun writeBanner() {
        val image = FontUtil.drawText(
            text = "Hello World! ggg",
            fontName = "BigSquareDots.ttf",
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
    fun testFigletText() {

        val sequence = XledSequence(
            fontName = "6x10",
            targetWidth = xledArrayLandscape.width,
            targetHeight = xledArrayLandscape.height,
            frameDelay = 100,
            Triple("Merry ", RGBColor(0, 0, 0), RGBColor(255, 255, 255)),
            Triple("Christmas", RGBColor(0, 0, 0), RGBColor(255, 0, 0)),
            Triple("!", RGBColor(0, 0, 0), RGBColor(255, 255, 255))
        )

        sequence.play(xledArrayLandscape)
    }

    @Test
    fun testLandscapeText() {
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
