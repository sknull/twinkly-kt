package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.games.conway.Conway
import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File

class XledArrayTest {

    private val xledArray = XledArray(listOf(
        XLedDevice("192.168.178.35", deviceOrigin = DeviceOrigin.TOP_LEFT),
        XLedDevice("192.168.178.52", deviceOrigin = DeviceOrigin.TOP_LEFT)
    ))

    @Test
    fun testPowerOn() {
        xledArray.powerOn()
    }

    @Test
    fun testPowerOff() {
        xledArray.powerOff()
    }

    @Test
    fun testDrawLine() {
        val canvas = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(0, 0, 0)
        )

        canvas.drawLine(1, 2, 18,10, RGBColor(255, 0, 0))
        canvas.drawLine(17, 3, 4,20, RGBColor(0, 255, 0))

        canvas.play(xledArray)
    }

    @Test
    fun testWhites() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(255, 255, 255)
        )

        frame.play(xledArray)
    }

    @Test
    fun testRGBColor() {
        val frame = XledFrame(
            width = xledArray.width,
            height = xledArray.height,
            initialColor = RGBColor(10, 0, 30)
        )

        xledArray.mode(DeviceMode.rt)
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

        xledArray.mode(DeviceMode.rt)
        frame.play(xledArray)
    }

    @Test
    fun testFade() {
        xledArray.mode(DeviceMode.rt)
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

        xledArray.mode(DeviceMode.rt)
        frame.play(xledArray)
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
            frame.play(xledArray)
            Thread.sleep(100)
        }

        // horizontal
        for (x in 0 until 20) {
            val frame = XledFrame(20, 21)
            for (y in 0 until 21) {
                frame[x][y] = RGBColor(255, 255, 255)
            }
            frame.play(xledArray)
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

        frameRed.play(xled = xledArray,)
    }

    @Test
    fun testShowRealtimeFrame() {
        val frame = XledFrame(File(ClassLoader.getSystemResource("images/smiley.png").toURI()))
        xledArray.mode(DeviceMode.rt)
        frame.play(xled = xledArray,)
    }

    @Test
    fun testSanta() {
        val frame = XledFrame(File(ClassLoader.getSystemResource("images/christmas-scenes/09_santa/frame_001.png").toURI()))
        xledArray.mode(DeviceMode.rt)
        frame.play(xled = xledArray,)
    }

    @Test
    fun testExpand() {
        val frame = XledFrame(8, 10, RGBColor(255, 0, 0))
        println(frame)
        frame.expandRight(5, RGBColor(0, 255, 0))
        frame.expandLeft(4, RGBColor(0, 0, 255))
        frame.expandTop(3, RGBColor(0, 255, 255))
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
    fun testChristmasTree() {
        val sequence = XledSequence(File(ClassLoader.getSystemResource("images/christmas-scenes/03_glitter").toURI()))
        xledArray.mode(DeviceMode.rt)
        sequence.play(xled = xledArray,)
    }

    @Test
    fun testChristmasScenes() {
        val sequence = XledSequence(
            File(ClassLoader.getSystemResource("images/christmas-scenes").toURI()),
            frameDelay = 5000
        )
        xledArray.mode(DeviceMode.rt)
        sequence.play(
            xled = xledArray,
            loop = -1,
            random = true,
//            transitionType = TransitionType.CURTAIN_CLOSE,
//            transitionDirection = TransitionDirection.HORIZONTAL,
            transitionBlendMode = BlendMode.REPLACE,
            transitionDuration = 1000
        )
    }

    @Test
    fun testLandscape() {
        val xledArrayLandscape = XledArray(
            xLedDevices = listOf(
                XLedDevice("192.168.178.35", deviceOrigin = DeviceOrigin.TOP_RIGHT),
                XLedDevice("192.168.178.52", deviceOrigin = DeviceOrigin.TOP_RIGHT)
            )
        )
        val canvas = XledFrame(xledArrayLandscape.width, xledArrayLandscape.height, RGBWColor(255,0, 0, 0))
        val green = XledFrame(2, xledArrayLandscape.height, RGBWColor(0,255, 0, 0))
        canvas.replaceSubFrame(green, 0, 0)
        val blue = XledFrame(2, xledArrayLandscape.height, RGBWColor(0,0, 255, 0))
        canvas.replaceSubFrame(blue, xledArrayLandscape.width - 2, 0)

        println(canvas)

        canvas.play(xledArrayLandscape)
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

        xledArray.mode(DeviceMode.rt)
        sequence.play(
            xled = xledArray,
            loop = -1,
            random = true,
//            transitionType = TransitionType.STRAIGHT,
//            transitionDirection = TransitionDirection.LEFT_RIGHT,
            transitionBlendMode = BlendMode.REPLACE,
            transitionDuration = 1000
        )
    }

    @Test
    fun testPacmanFrame() {
        val frame = XledFrame(File(ClassLoader.getSystemResource("images/pacman/pacman_000.png").toURI()))
        xledArray.mode(DeviceMode.rt)
        frame.play(xled = xledArray,)
    }

    @Test
    fun testPacman() {
        val sequence = XledSequence(File(ClassLoader.getSystemResource("images/pacman").toURI()))

        sequence.play(xled = xledArray, loop = 0,)
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