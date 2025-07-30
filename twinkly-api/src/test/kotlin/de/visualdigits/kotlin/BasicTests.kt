package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import kotlin.math.min
import kotlin.random.Random

@Disabled("only for local testing")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class BasicTests : XledArrayTest() {

//    @Test
//    fun testRunningPixel() {
//        xledMatrix.powerOn()
//        for (x in 0 until 10) {
//            for (y in 0 until 50) {
//                val frame = XledFrame(
//                    width = xledMatrix.width,
//                    height = xledMatrix.height
//                )
//                frame[x, y] = RGBColor(255, 255, 255)
//                frame.play(xledMatrix)
//                Thread.sleep(10)
//            }
//        }
//    }
//
//    @Test
//    fun testPixel() {
//        xledArray.powerOn()
//        val frame = XledFrame(
//            width = xledArray.width,
//            height = xledArray.height
//        )
//        frame[0, 0] = RGBColor(255, 255, 255)
//        frame.play(xledArray)
//    }

    @Test
    @Order(1)
    fun testPowerOn() {
        xledArray.powerOn()
    }

    @Test
    @Order(2)
    fun testBrightness() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(255, 255, 0)
        )

        xledArray.setBrightness(0.1f)
        frame.play(xledArray)
    }

    @Test
    @Order(3)
    fun testSaturation() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(255, 0, 0)
        )

        xledArray.setBrightness(1.0f)
        xledArray.setSaturation(1.0f)
        frame.play(xledArray)
    }

    @Test
    @Order(4)
    fun testMovingStripes() {
        // vertical
        for (y in 0 until xledArray.height) {
            val frame = XledFrame(xledArray.width, xledArray.height)
            for (x in 0 until xledArray.width) {
                frame[x, y] = RGBColor(255, 255, 255)
            }
            frame.play(xledArray)
            Thread.sleep(30)
        }

        // horizontal
        for (x in 0 until xledArray.width) {
            val frame = XledFrame(xledArray.width, xledArray.height)
            for (y in 0 until xledArray.height) {
                frame[x, y] = RGBColor(255, 255, 255)
            }
            frame.play(xledArray)
            Thread.sleep(30)
        }
    }

    @Test
    @Order(5)
    fun testPatternHorizontal() {
        // vertical
        val frame = XledFrame(xledArray.width, xledArray.height)
        for (y in 0 until xledArray.height step 3) {
            for (x in 0 until xledArray.width) {
                frame[x, y] = RGBColor(255, 0, 0)
                frame[x, y + 1] = RGBColor(255, 255, 255)
                frame[x, y + 2] = RGBColor(0, 0, 255)
            }
        }
        frame.play(xledArray)
    }

    @Test
    @Order(6)
    fun testPatternVertical() {
        // vertical
        val frame = XledFrame(xledArray.width, xledArray.height)
        for (y in 0 until xledArray.height) {
            for (x in 0 until xledArray.width step 3) {
                frame[x, y] = RGBColor(255, 0, 0)
                frame[x + 1, y] = RGBColor(255, 255, 255)
                frame[x + 2, y] = RGBColor(0, 0, 255)
            }
        }
        frame.play(xledArray)
    }

    @Test
    @Order(7)
    fun testNativeWhite() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBWColor(0, 0, 0, 255)
        )

        frame.play(xledArray)
    }

    @Test
    @Order(8)
    fun testRGBWhite() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBWColor(255, 255, 255, 0)
        )

        frame.play(xledArray)
    }

    @Test
    @Order(9)
    fun testCombinedWhite() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(255, 255, 255)
        )

        frame.play(xledArray)
    }

    @Test
    @Order(10)
    fun testHsv() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = HSVColor(0, 100, 100)
        )

        frame.play(xledArray)
    }

    @Test
    @Order(11)
    fun testRed() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(255, 0, 0)
        )

        frame.play(xledArray)
    }

    @Test
    @Order(12)
    fun testGreen() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(0, 255, 0)
        )

        frame.play(xledArray)
    }

    @Test
    @Order(13)
    fun testBlue() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(0, 0, 255)
        )

        frame.play(xledArray)
    }

    @Test
    @Order(14)
    fun testDrawLines() {
        val canvas = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(0, 0, 0)
        )

        canvas.drawLine(0, 0, xledArray.width - 1,xledArray.height - 1, RGBColor(255, 0, 0))
        canvas.drawLine(xledArray.width / 4, xledArray.height - 3, xledArray.width - 3,3, RGBColor(0, 255, 0))

        canvas.play(xledArray)
    }

    @Test
    @Order(15)
    fun testDrawRectangles() {
        val canvas = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(0, 0, 0)
        )

        canvas.drawRect(0, 0, xledArray.width - 1, xledArray.height - 1, RGBColor(255, 0, 0))
        canvas.drawRect(5, 5, xledArray.width - 6, xledArray.height - 6, RGBColor(0, 255, 0))

        canvas.play(xledArray)
    }

    @Test
    @Order(16)
    fun testDrawCircles() {
        val canvas = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(0, 0, 0)
        )

        val random = Random(System.currentTimeMillis())
        val cx = xledArray.width / 2
        val cy = xledArray.height / 2
        for (r in 3 until min(xledArray.width / 2, xledArray.height / 2) step 3) {
            val color = RGBColor(random.nextInt(0, 256), random.nextInt(0, 256), random.nextInt(0, 256))
            canvas.drawCircle(cx, cy, r, r, color)
        }

        canvas.play(xledArray)
    }

    @Test
    @Order(99)
    fun testPowerOff() {
        xledArray.powerOff()
    }
}
