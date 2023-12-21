package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import org.junit.jupiter.api.Test

class BasicTests : XledArrayTest() {

    @Test
    fun testPowerOn() {
        xledArray.powerOn()
    }

    @Test
    fun testPowerOff() {
        xledArray.powerOff()
    }

    @Test
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
    fun testNativeWhite() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBWColor(0, 0, 0, 255)
        )

        frame.play(xledArray)
    }

    @Test
    fun testRGBWhite() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBWColor(255, 255, 255, 0)
        )

        frame.play(xledArray)
    }

    @Test
    fun testCombinedWhite() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(255, 255, 255)
        )

        frame.play(xledArray)
    }

    @Test
    fun testRed() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(255, 0, 0)
        )

        frame.play(xledArray)
    }

    @Test
    fun testGreen() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(0, 255, 0)
        )

        frame.play(xledArray)
    }

    @Test
    fun testBlue() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(0, 0, 255)
        )

        frame.play(xledArray)
    }
}