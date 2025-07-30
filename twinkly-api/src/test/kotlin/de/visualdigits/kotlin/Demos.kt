package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.Rotation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionType
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openqa.selenium.chrome.ChromeDriver
import java.io.File
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Disabled("only for local testing")
class Demos : XledArrayTest() {

    @Test
    fun liveScoreBoard() {
        while (true) {
            val scoreFrame = createScoreFrame()
            scoreFrame.play(xled = xledArray)

            for (i in 1 .. 6) {
                Thread.sleep(5000)
                xledArray.setMode(DeviceMode.rt)
            }
        }
    }

    @Test
    fun liveScore() {
        val result = getLiveScore()
        println(result)
    }

    private fun createScoreFrame(): XledFrame {
        val score = getLiveScore()
        val canvas = XledFrame(xledArray.width, xledArray.height)
        if (score.isNotEmpty()) {
            val flag1 = XledFrame(File(ClassLoader.getSystemResource("images/flags/euro24/${score[0].first}.png").toURI()))
            val goals1 = XledFrame(
                text = score[0].second,
                fontName = "xttyb",
                backgroundColor = RGBColor(0, 0, 0),
                textColor = RGBColor(255, 255, 255)
            )

            val flag2 = XledFrame(File(ClassLoader.getSystemResource("images/flags/euro24/${score[1].first}.png").toURI()))
            val goals2 = XledFrame(
                text = score[1].second,
                fontName = "xttyb",
                backgroundColor = RGBColor(0, 0, 0),
                textColor = RGBColor(255, 255, 255)
            )

            canvas.replaceSubFrame(flag1, 0, 0)
            canvas.replaceSubFrame(goals1, (xledArray.width - goals1.width) / 2, 9, BlendMode.ADD)

            canvas.replaceSubFrame(flag2, 0, 21)
            canvas.replaceSubFrame(goals2, (xledArray.width - goals2.width) / 2, 30, BlendMode.ADD)
        }
        return canvas
    }

    private fun getLiveScore(): List<Pair<String, String>> {
        val url = "https://de.uefa.com/euro2024/fixtures-results/"
        val driver = ChromeDriver()
        driver.get(url)
        Thread.sleep(1000)
        val html = driver.pageSource
        driver.quit()
//        val html = File(ClassLoader.getSystemResource("html/scrape-pending.html").toURI()).readText()
        return "Live-Ergebnisse - (.*?)\"".toRegex()
            .find(html)
            ?.let { a ->
                a.groups[1]?.value
                    ?.split(" - ")
                    ?.let { b ->
                        "${b[0]} (\\d+)-(\\d+) ${b[1]}".toRegex()
                            .find(html)
                            ?.let { c ->
                                listOf(
                                    Pair(b[0], c.groups[1]?.value ?: ""),
                                    Pair(b[1], c.groups[2]?.value ?: "")
                                )
                            }?:listOf(Pair(b[0], "0"),Pair(b[1], "0"))
                    }
            } ?: "Nächstes Spiel - (.*?)\"".toRegex().find(html)?.let { d ->
            d.groups[1]?.value?.split(" - ")?.let { e -> listOf(Pair(e[0], "?"), Pair(e[1], "?")) }
        } ?: listOf()
    }

    /**
     * Shows a clock with local time and date
     */
    @Test
    fun testClock() {
//        val device = xledArrayLandscapeRight
        val device = xledMatrix
        val canvas = XledFrame(device.height, device.width, rotation = Rotation.LEFT)
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
            canvas.replaceSubFrame(frameDate, 0 , device.height / 2)
            val dayMarker = (now.hour / 24.0 * device.width).roundToInt()
            for (x in 0 until dayMarker) {
                canvas[x, 0] = RGBColor(0, 255, 0)
            }
            val yearMarker = (OffsetDateTime.now().dayOfYear / 365.0 * device.width).roundToInt()
            for (x in 0 until yearMarker) {
                canvas[x, device.height - 1] = RGBColor(255, 255, 0)
            }
            canvas.play(device)

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
    fun testScene() {
        val frame = XledFrame(File(ClassLoader.getSystemResource("images/christmas-scenes/14_Lights/frame_001.png").toURI()))
        frame.play(xled = xledArray)
    }

    @Test
    fun testChristmasScenes() {
        val blackout = XledFrame(xledArray.width, xledArray.height)
        blackout.play(xledArray)
        val sequence = XledSequence(
            File(ClassLoader.getSystemResource("images/christmas-scenes").toURI()),
            initialColor = RGBColor(0, 0, 0),
            frameDelay = 5000
        )
        sequence.play(
            xled = xledArray,
            loop = -1,
            transitionType = TransitionType.RANDOM,
//            transitionDirection = TransitionDirection.HORIZONTAL,
            randomSequence = true,
            transitionDuration = 1000
        )
    }

    @Test
    fun testText() {
        val device = xledArrayLandscapeRight
//        val device = xledMatrix
        val sequence = XledSequence(
            fontName = "Only When I Do Fonts Regular.ttf",
            fontDirectory = File(ClassLoader.getSystemResource("fonts").toURI()),
            fontSize = 10,
            targetWidth = device.height,
            targetHeight = device.width,
            frameDelay = 20,
            texts = listOf(
                Triple("Merry ", RGBColor(0, 0, 0), RGBColor(255, 255, 255)),
                Triple("Christmas", RGBColor(0, 0, 0), RGBColor(255, 0, 0)),
                Triple("!", RGBColor(0, 0, 0), RGBColor(255, 255, 255))
            ),
            rotation = Rotation.LEFT
        )

        sequence.play(device, -1)
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
}
