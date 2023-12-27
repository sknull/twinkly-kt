package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import de.visualdigits.kotlin.twinkly.visualization.Oscilloscope
import de.visualdigits.kotlin.twinkly.visualization.SpectrumAnalyzer
import de.visualdigits.kotlin.twinkly.visualization.VUMeter
import de.visualdigits.kotlin.twinkly.visualization.Visualizer
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class Demos : XledArrayTest() {

    /**
     * Shows a clock with local time and date
     */
    @Test
    fun testClock() {
        val canvas = XledFrame(xledArrayLandscapeRight.width, xledArrayLandscapeRight.height)
        val bgColor = RGBColor(0, 0, 0)
        val digitColor = RGBColor(255, 0, 0)
        val doubleColonColor = RGBColor(0, 0, 128)
        val separatorTime = ":"
        val separatorDate = "."

        while (true) {
            var now = LocalDateTime.now()
            val frameTime = XledFrame(
                fontName = "xttyb",
                texts = listOf(
                    Triple(now.format(DateTimeFormatter.ofPattern("HH")), bgColor, digitColor),
                    Triple(separatorTime, bgColor, doubleColonColor),
                    Triple(now.format(DateTimeFormatter.ofPattern("mm")), bgColor, digitColor),
                    Triple(separatorTime, bgColor, doubleColonColor),
                    Triple(now.format(DateTimeFormatter.ofPattern("ss")), bgColor, digitColor),
                )
            )
            val frameDate = XledFrame(
                fontName = "xtty",
                texts = listOf(
                    Triple(now.format(DateTimeFormatter.ofPattern("dd")), bgColor, digitColor),
                    Triple(separatorDate, bgColor, doubleColonColor),
                    Triple(now.format(DateTimeFormatter.ofPattern("MM")), bgColor, digitColor),
                    Triple(separatorDate, bgColor, doubleColonColor),
                    Triple(now.format(DateTimeFormatter.ofPattern("yy")), bgColor, digitColor),
                )
            )
//            if (separator == ":") separator = " " else separator = ":"
            canvas.replaceSubFrame(frameTime)
            canvas.replaceSubFrame(frameDate, 0 , xledArrayLandscapeRight.height / 2)
            val dayMarker = (now.hour / 24.0 * xledArrayLandscapeRight.width).roundToInt()
            for (x in 0 until dayMarker) {
                canvas[x, 0] = RGBColor(0, 255, 0)
            }
            val yearMarker = (OffsetDateTime.now().dayOfYear / 365.0 * xledArrayLandscapeRight.width).roundToInt()
            for (x in 0 until yearMarker) {
                canvas[x, xledArrayLandscapeRight.height - 1] = RGBColor(255, 255, 0)
            }
            canvas.play(xledArrayLandscapeRight, 1)

            Thread.sleep(100)
        }
    }

    @Test
    fun testTwinklyChristmasTree() {
        val sequence = XledSequence(frameDelay = 300)
            .addImagesFromDirectory(File(ClassLoader.getSystemResource("images/christmas-scenes/03_glitter").toURI()))
        sequence.play(xledArray, -1)
    }

    @Test
    fun testSanta() {
        val frame = XledFrame(File(ClassLoader.getSystemResource("images/christmas-scenes/09_santa/frame_001.png").toURI()))
        frame.play(xled = xledArray)
    }

    @Test
    fun testChristmasScenes() {
        val blackout = XledFrame(xledArray.width, xledArray.height)
        blackout.play(xledArray)
        val sequence = XledSequence(
            File(ClassLoader.getSystemResource("images/christmas-scenes").toURI()),
            initialColor = RGBColor(0, 0, 0),
            frameDelay = 30000
        )
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
    fun testOscilloscope() {
        val oscilloscope = Oscilloscope(
            xled = xledArrayLandscapeRight
        )
        oscilloscope.run()
    }

    @Test
    fun testSpectrumAnalyzer() {
        val analyzer = SpectrumAnalyzer(
            xled = xledArrayLandscapeRight
        )
        analyzer.run()
    }

    @Test
    fun testVUMeter() {
        val vuMeter = VUMeter(
            xled = xledArray
        )
        vuMeter.run()
    }

    @Test
    fun testVisualizer() {
        val visualizer = Visualizer(
            xled = xledArray
        )
        visualizer.run()
    }
}
