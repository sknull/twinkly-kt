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
            fontName = "Only When I Do Fonts Regular.ttf",
            fontDirectory = File(ClassLoader.getSystemResource("fonts").toURI()),
            fontSize = 20,
            targetWidth = xledArrayLandscapeRight.width,
            targetHeight = xledArrayLandscapeRight.height,
            frameDelay = 30,
            texts = listOf(
                Triple("Merry ", RGBColor(0, 0, 0), RGBColor(255, 255, 255)),
                Triple("Christmas", RGBColor(0, 0, 0), RGBColor(255, 0, 0)),
                Triple("!", RGBColor(0, 0, 0), RGBColor(255, 255, 255))
            )
        )

        sequence.play(xledArrayLandscapeRight, -1)
    }

    @Test
    fun testFigletText() {
        val sequence = XledSequence(
            fontName = "6x10",
            targetWidth = xledArrayLandscapeRight.width,
            targetHeight = xledArrayLandscapeRight.height,
            frameDelay = 30,
            texts = listOf(
                Triple("Merry ", RGBColor(0, 0, 0), RGBColor(255, 255, 255)),
                Triple("Christmas", RGBColor(0, 0, 0), RGBColor(255, 0, 0)),
                Triple("!", RGBColor(0, 0, 0), RGBColor(255, 255, 255))
            )
        )

        sequence.play(xledArrayLandscapeRight)
    }

    @Test
    fun testLandscapeText() {
        val sequence = XledSequence(
            fontName = "fonts/Only When I Do Fonts Regular.ttf",
            fontSize = 10,
            targetWidth = xledArrayLandscapeLeft.width,
            targetHeight = xledArrayLandscapeLeft.height,
            frameDelay = 100,
            texts = listOf(
                Triple("Merry ", RGBColor(0, 0, 0), RGBColor(255, 255, 255)),
                Triple("Christmas", RGBColor(0, 0, 0), RGBColor(255, 0, 0)),
                Triple("!", RGBColor(0, 0, 0), RGBColor(255, 255, 255)))
        )

        sequence.play(xledArrayLandscapeLeft)
    }
}
