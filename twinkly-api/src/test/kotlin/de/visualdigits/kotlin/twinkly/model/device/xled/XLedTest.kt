package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.request.CurrentMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.request.NewMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.LedMode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.LedMovieConfigResponse
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import de.visualdigits.kotlin.util.TimeUtil
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.random.Random.Default.nextInt

@Disabled("only for local testing")
class XLedTest {

    private val xled = XLedDevice.instance(ipAddress = "192.168.178.38", name = "xled", width = 10, height = 50)

    protected val xledMatrix = XledMatrixDevice.instance("192.168.178.39", name = "matrix", 10, 50)

    @Test
    fun testDeviceInfo() {
//        println(xled.getLedMovieConfig())
        println(xled.deviceInfo)
//        println(xled.firmwareVersion)
//        println(xledMatrix.status())
//        println(xledMatrix.networkStatus())
//        println(xledMatrix.getMusicEnabled())
//        println(xledMatrix.getMusicDriversCurrent())
//        println(xledMatrix.getMusicDriversSets())
//        println(xledMatrix.getCurrentMusicDriversSet())
//        println(xledMatrix.getCurrentMovie())
//        println(xledMatrix.getMovies())
    }

    @Test
    fun testMusicStats() {
        while(true) {
            println(xled.getLedMusicStats())
            Thread.sleep(100)
        }
    }

    @Disabled("Adding a new movie does not really work but might mess up an existing movie.")
    @Test
    fun testNewMovie() {
        val deviceInfo = xled.deviceInfo

        xled.setColor(RGBColor())
        xled.setLedMode(LedMode.color)
        xled.deleteMovies()
        val newMovie = xled.uploadNewMovie(
            NewMovieRequest(
                name = "FooBar",
                descriptorType = "rgbw_raw",
                ledsPerFrame = deviceInfo?.numberOfLed?:4,
                framesNumber = 1,
                fps = 1
            )
        )
        println(newMovie)
        xled.uploadNewMovieToListOfMovies(XledFrame(10, 21, RGBColor(nextInt(0, 255), nextInt(0, 255), nextInt(0, 255))))
        xled.setLedMovieConfig(
            LedMovieConfigResponse(
                frameDelay = 1000 / 1,
                ledsNumber = deviceInfo?.numberOfLed?:4,
                framesNumber = 1,
            )
        )
        xled.setLedMode(LedMode.movie)
        println(xled.getMovies())
        println(xled.getCurrentMovie())
        println(xled.getPlaylist())
        println(xled.getCurrentPlaylist())
        println(xled.getLedEffects())
        println(xled.getCurrentLedEffect())
    }

    @Test
    fun testTimer() {
        xled.setTimer(10, 35, 10, 36)
        val timer = xled.getTimer()
        println(timer)
    }

    @Test
    fun testTimer2() {
        val timer = xled.getTimer()
        val t = timer?.timeNow?.let { TimeUtil.offsetDateTimeOfSecondsFromMidnight(it) }
        println(t)
    }

    @Test
    fun testRandomColorMovie() {
        val frame = XledFrame(10, 50, RGBColor(nextInt(0, 255), nextInt(0, 255), nextInt(0, 255)))
        frame.play(xled = xled, loop = -1)
    }

    @Test
    fun testImage() {
        val frame = XledFrame(File(ClassLoader.getSystemResource("images/test-image.png").toURI()))
        frame.play(xled = xled, loop = -1)
    }

    @Test
    fun testAnimatedGif() {
        val sequence = XledSequence(frameDelay = 500)
        ClassLoader.getSystemResourceAsStream("images/tetris/Animation.gif")
            ?.use { ins -> sequence.addAnimatedGif(ins, maxFrames = 200) }
        sequence.play(xled = xled, loop = -1)
    }

    @Test
    fun testDirectoryTetris() {
        val sequence = XledSequence(
            directory = File(ClassLoader.getSystemResource("images/tetris").toURI()),
            frameDelay = 500,
            maxFrames = 200
        )
        xled.showSequence(
            "Tetris", sequence, 2
        )
    }

    @Test
    fun testDirectoryChristmas() {
        val sequence = XledSequence(
            directory = File(ClassLoader.getSystemResource("images/christmas").toURI()),
            frameDelay = 500,
            maxFrames = 200
        )
        sequence.play(xled = xled, loop = -1)
    }

    @Test
    fun testDirectoryChristmasTree() {
        val sequence = XledSequence(
            directory = File(ClassLoader.getSystemResource("images/christmas-tree").toURI()),
            frameDelay = 500,
            maxFrames = 200
        )
        sequence.play(xled = xled, loop = -1)
    }

    @Test
    fun testColor() {
        xled.setColor(RGBColor(0, 0, 0))
        xled.setLedMode(LedMode.color)
        xled.deleteMovies()

        val fps = 1

        val sequence = XledSequence()

        var frame = XledFrame(xled.width, xled.height, RGBWColor(255, 255, 255, 128, normalize = false))
//        playable.setImage(File(ClassLoader.getSystemResource("images/tetris/teris-0.jpg").toURI()))
        sequence.add(frame)
//        playable = XledFrame(columns, rows, bytesPerLed, RGBColor(0, 0, 0))
//        playable.setImage(File(ClassLoader.getSystemResource("images/tetris/teris-1.jpg").toURI()))
//        sequence.add(playable)

//        val ins = ClassLoader.getSystemResourceAsStream("images/HelloWorld!.png")
//        val image = ImageIO.read(ins)
//        for (f in 0 until image.width - columns) {
//            val playable = XledFrame(columns, rows, bytesPerLed, RGBColor(0, 0, 0))
//            val imageFrame = image.getSubimage(f, 0, columns, rows)
//            playable.setImage(imageFrame)
//            sequence.add(playable)
//        }

//        File(ClassLoader.getSystemResource("images/tetris").toURI())
//            .listFiles { file -> file.name.lowercase().endsWith(".png") }
//            ?.forEach { file ->
//                println("### file: ${file.name}")
//                val playable = XledFrame(columns, rows, bytesPerLed)
//                playable.setImage(file)
//                sequence.add(playable)
//            }

        val numberOfFrames = sequence.size
        val newMovie = xled.uploadNewMovie(
            NewMovieRequest(
                name = "Foo",
                descriptorType = "rgbw_raw",
                ledsPerFrame = xled.deviceInfo?.numberOfLed?:4,
                framesNumber = numberOfFrames,
                fps = fps
            )
        )
        xled.uploadNewMovieToListOfMovies(sequence.toByteArray(xled.bytesPerLed))
        xled.setLedMovieConfig(
            LedMovieConfigResponse(
                frameDelay = 1000 / fps,
                ledsNumber = xled.deviceInfo?.numberOfLed?:4,
                framesNumber = numberOfFrames,
            )
        )
        xled.setCurrentMovie(
            CurrentMovieRequest(
                id = newMovie?.id
            )
        )
        xled.setLedMode(LedMode.movie)
    }
}
