package de.visualdigits.kotlin.twinkly.model

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.games.conway.Conway
import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.frame.XledSequence
import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import org.junit.jupiter.api.Test
import java.io.File

class XledArrayTest {

    private val xledArray = XledArray(listOf(
        XLedDevice("192.168.178.35"),
        XLedDevice("192.168.178.52")
    ))

    @Test
    fun testWhites() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(255, 255, 255)
        )

        xledArray.mode(DeviceMode.rt)
        xledArray.showRealTimeFrame(frame)
    }

    @Test
    fun testColorMix() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height
        )
        val red = XledFrame(
            width = 10,
            height = 10,
            initialColor = RGBColor(255, 0, 0)
        )
        val green = XledFrame(
            width = 10,
            height = 10,
            initialColor = RGBColor(0, 255, 0)
        )
        frame.replaceSubFrame(red, 5, 5, BlendMode.ADD)
        frame.replaceSubFrame(green, 10, 10, BlendMode.ADD)

        xledArray.mode(DeviceMode.rt)
        xledArray.showRealTimeFrame(frame)
    }

    @Test
    fun testFade() {
        val frame = XledFrame.fromImage(File(ClassLoader.getSystemResource("images/smiley.png").toURI()))

        xledArray.mode(DeviceMode.rt)
        xledArray.showRealTimeFrame(frame)
        Thread.sleep(1000)

        frame.fade(RGBColor(255,255,0), 2000, xledArray)
    }

    @Test
    fun testBlendModes() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height
        )

        val red = XledFrame(10, 10, RGBColor(255,0, 0))
        frame.replaceSubFrame(red, 0, 0)

        val green = XledFrame(10, 10, RGBColor(0,255, 0))

        val transparent = XledFrame(2, 2, RGBColor(0,0, 0, 0))
        green.replaceSubFrame(transparent, 1, 1)

        frame.replaceSubFrame(green, 5, 5)

        xledArray.mode(DeviceMode.rt)
        xledArray.showRealTimeFrame(frame)
    }

    @Test
    fun testMovingStripes() {
        // vertical
        xledArray.mode(DeviceMode.rt)
        for (y in 0 until 21) {
            val frame = XledFrame(20, 21)
            for (x in 0 until 20) {
                frame[x][y] = RGBColor(255, 255, 255)
            }
            xledArray.showRealTimeFrame(frame)
            Thread.sleep(100)
        }

        // horizontal
        for (x in 0 until 20) {
            val frame = XledFrame(20, 21)
            for (y in 0 until 21) {
                frame[x][y] = RGBColor(255, 255, 255)
            }
            xledArray.showRealTimeFrame(frame)
            Thread.sleep(100)
        }
    }

    @Test
    fun testSubFrame() {
        val frameRed = XledFrame(xledArray.width, xledArray.height, RGBColor(255,0, 0))
        val frameGreen = XledFrame(10, 10, RGBColor(0,255, 0))
        val frameBlue = XledFrame(10, 10, RGBColor(0,0, 255))
        val frameYellow = XledFrame(10, 10, RGBColor(255,255, 0))
        val frameMagenta = XledFrame(10, 10, RGBColor(255,0, 255))
        val frameCyan = XledFrame(10, 10, RGBColor(0,255, 255))

        frameRed.replaceSubFrame(frameGreen, -5, -5)
        frameRed.replaceSubFrame(frameBlue, 15, -5)
        frameRed.replaceSubFrame(frameYellow, -5, 15)
        frameRed.replaceSubFrame(frameMagenta, 15, 15)
        frameRed.replaceSubFrame(frameCyan, 5, 5)

        xledArray.mode(DeviceMode.rt)
        xledArray.showRealTimeFrame(frameRed)
    }

    @Test
    fun testShowRealtimeFrame() {
        xledArray.mode(DeviceMode.rt)
        val frame = XledFrame.fromImage(File(ClassLoader.getSystemResource("images/smiley.png").toURI()))
        xledArray.showRealTimeFrame(frame)
    }

    @Test
    fun testTwinklyChristmasTree() {
        xledArray.mode(DeviceMode.rt)
        val sequence = XledSequence.fromDirectory(File(ClassLoader.getSystemResource("images/glitter").toURI()))
        while(true) {
            xledArray.showRealTimeSequence(sequence, 100)
        }
    }

    @Test
    fun testTwinklyPacmanFrame() {
        xledArray.mode(DeviceMode.rt)
        xledArray.showRealTimeFrame(XledFrame.fromImage(File(ClassLoader.getSystemResource("images/pacman/pacman_000.png").toURI())))
    }

    @Test
    fun testTwinklyPacman() {
        xledArray.mode(DeviceMode.rt)
        val sequence = XledSequence.fromDirectory(File(ClassLoader.getSystemResource("images/pacman").toURI()))
        while(true) {
            xledArray.showRealTimeSequence(sequence, 300)
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
