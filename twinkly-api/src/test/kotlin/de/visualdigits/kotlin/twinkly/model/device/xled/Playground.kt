package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.XledArrayTest
import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled("only for local testing")
class Playground : XledArrayTest() {

    @Test
    fun testRGBColor() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(10, 0, 30)
        )

        xledArray.setMode(DeviceMode.rt)
        frame.play(xledArray)
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

        xledArray.setMode(DeviceMode.rt)
        frame.play(xledArray)
    }

    @Test
    fun testFade() {
        xledArray.setMode(DeviceMode.rt)
        var frame = XledFrame(xledArray.width, xledArray.height, RGBColor(255, 0, 0))
        var black = XledFrame(xledArray.width, xledArray.height, RGBColor(0, 0, 0))
        var f = 0.0
        for (i in 0 .. 1000) {
            xledArray.showRealTimeFrame(frame)
            frame = frame.fade(black, 1.0 / 100 * i)
            f += 0.01
            if (f > 1.0) {
                f = 0.0
            }
            Thread.sleep(100)
        }
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

        xledArray.setMode(DeviceMode.rt)
        frame.play(xledArray)
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

        frameRed.play(xled = xledArray,)
    }

    @Test
    fun testShowRealtimeFrame() {
        val frame = XledFrame(File(ClassLoader.getSystemResource("images/smiley.png").toURI()))
        frame.play(xled = xledArray,)
    }

    @Test
    fun testSanta() {
        val frame = XledFrame(File(ClassLoader.getSystemResource("images/christmas-scenes/09_santa/frame_001.png").toURI()))
        frame.play(xled = xledArray)
    }

    @Test
    fun testExpand() {
        val frame = XledFrame(8, 10, RGBColor(255, 0, 0))
        println(frame)
        frame.expandRight(5, RGBColor(0, 255, 0))
        println(frame)
        frame.expandLeft(4, RGBColor(0, 0, 255))
        println(frame)
        frame.expandTop(3, RGBColor(0, 255, 255))
        println(frame)
        frame.expandBottom(2, RGBColor(255, 0, 255))
        println(frame)

    }

    @Test
    fun testExpandFrame() {
        val frame = XledFrame(8, 10, RGBColor(255, 0, 0))
        val green = XledFrame(5, 12, RGBColor(0, 255, 0))
        val blue = XledFrame(4, 8, RGBColor(0, 0, 255))
        val cyan = XledFrame(10, 2, RGBColor(0, 255, 255))
        val yellow = XledFrame(4, 3, RGBColor(255, 255, 0))
        frame.expandRight(green)
        frame.expandLeft(blue)
        frame.expandTop(cyan)
        frame.expandBottom(yellow)
        println(frame)
    }

    @Test
    fun testLandscape() {
        val canvas = XledFrame(xledArrayLandscapeLeft.width, xledArrayLandscapeLeft.height, RGBWColor(255,0, 0, 0))
        val green = XledFrame(2, xledArrayLandscapeLeft.height, RGBWColor(0,255, 0, 0))
        canvas.replaceSubFrame(green, 0, 0)
        val blue = XledFrame(2, xledArrayLandscapeLeft.height, RGBWColor(0,0, 255, 0))
        canvas.replaceSubFrame(blue, xledArrayLandscapeLeft.width - 2, 0)

        println(canvas)

        canvas.play(xledArrayLandscapeLeft)
    }

    @Test
    fun testWipe() {
        val sequence = XledSequence(frameDelay = 1000, frames = mutableListOf(
            XledFrame(xledArray.width, xledArray.height, RGBWColor(255,0, 0, 0)),
            XledFrame(xledArray.width, xledArray.height, RGBWColor(0,255, 0, 0)),
            XledFrame(xledArray.width, xledArray.height, RGBWColor(0,0, 255, 0)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(255,255, 0, 0)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(0,255, 255, 0)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(255,0, 255, 0)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(255,255, 255, 0)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(255,0, 0, 255)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(0,255, 0, 255)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(0,0, 255, 255)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(255,255, 0, 255)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(0,255, 255, 255)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(255,0, 255, 255)),
//            XledFrame(xledArray.width, xledArray.height, RGBWColor(255,255, 255, 255)),
        ))

//        val sequence = XledSequence(frameDelay = 1000)
//        for (n in 0 until 256) {
//            val random = Random(System.currentTimeMillis())
//            sequence.add(XledFrame(xledArray.width, xledArray.height, RGBWColor(
//                red = random.nextInt(0, 255),
//                green = random.nextInt(0, 255),
//                blue = random.nextInt(0, 255),
//                white = random.nextInt(0, 255)
//            )))
//        }

        xledArray.setMode(DeviceMode.rt)
        sequence.play(
            xled = xledArray,
            loop = -1,
            //            transitionType = TransitionType.STRAIGHT,
//            transitionDirection = TransitionDirection.LEFT_RIGHT,
            transitionDuration = 1000
        )
    }

    @Test
    fun testPacmanFrame() {
        val frame = XledFrame(File(ClassLoader.getSystemResource("images/pacman/pacman_000.png").toURI()))
        xledArray.setMode(DeviceMode.rt)
        frame.play(xled = xledArray,)
    }

    @Test
    fun testPacman() {
        val sequence = XledSequence(File(ClassLoader.getSystemResource("images/pacman").toURI()))

        sequence.play(xled = xledArray, loop = 0,)
    }

    @Test
    fun testColors() {
        xledArray.setColor(RGBWColor(0, 0, 0, 255))
    }
}
