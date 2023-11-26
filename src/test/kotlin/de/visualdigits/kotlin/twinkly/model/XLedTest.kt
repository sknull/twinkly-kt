package de.visualdigits.kotlin.twinkly.model

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.conway.Conway
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.frame.XledSequence
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.xled.request.MoviesCurrentRequest
import de.visualdigits.kotlin.twinkly.model.xled.request.NewMovieRequest
import de.visualdigits.kotlin.twinkly.model.xled.response.MovieConfig
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.random.Random.Default.nextInt

class XLedTest {

    private val xled = XLedDevice("192.168.178.35")

    @Test
    fun testDeviceInfo() {
        println(xled.deviceInfo)
        println(xled.status())
        println(xled.networkStatus())
        println(xled.musicEnabled())
        println(xled.musicDriversCurrent())
        println(xled.musicDriversSets())
        println(xled.currentMusicDriversSet())
    }

    @Test
    fun testMusicStats() {
        while(true) {
            println(xled.musicStats())
            Thread.sleep(100)
        }
    }

    @Test
    fun testConwaysGameOfLife() {
        val conway = Conway(
            preset = File(ClassLoader.getSystemResource("conway/conway_diehard.png").toURI()),
            xled = xled
        )
        conway.run()
    }

    @Test
    fun testRealTime() {
        xled.mode(DeviceMode.rt)
        File(ClassLoader.getSystemResource("images/tetris").toURI())
            .listFiles { file -> file.isFile && file.name.lowercase().endsWith(".png") }
            ?.forEach { file ->
                xled.showRealTimeFrame(XledFrame.fromImage(file))
                Thread.sleep(20)
            }
    }

    @Test
    fun testNewMovie() {
        val deviceInfo = xled.deviceInfo()

        xled.color(RGBColor())
        xled.mode(DeviceMode.color)
        xled.deleteMovies()
        val newMovie = xled.newMovie(
            NewMovieRequest(
                name = "FooBar",
                descriptorType = "rgbw_raw",
                ledsPerFrame = deviceInfo.numberOfLed,
                framesNumber = 1,
                fps = 1
            )
        )
        println(newMovie)
        xled.uploadMovie(XledFrame(10, 21, RGBColor(nextInt(0, 255), nextInt(0, 255), nextInt(0, 255))))
        xled.movieConfig(
            MovieConfig(
                frameDelay = 1000 / 1,
                ledsNumber = deviceInfo.numberOfLed,
                framesNumber = 1,
            )
        )
        xled.mode(DeviceMode.movie)
        println(xled.movies())
        println(xled.moviesCurrent())
        println(xled.playlist())
        println(xled.playlistCurrent())
        println(xled.effects())
        println(xled.effectsCurrent())
    }

    @Test
    fun testRandomColorMovie() {
        xled.showFrame("Blah", XledFrame(10, 21, RGBColor(nextInt(0, 255), nextInt(0, 255), nextInt(0, 255))))
    }

    @Test
    fun testImage() {
        xled.showFrame("Test", XledFrame.fromImage(File(ClassLoader.getSystemResource("images/test-image.png").toURI())))
    }

    @Test
    fun testAnimatedGif() {
        xled.showSequence(name = "Tetris",
            sequence = XledSequence.fromAnimatedGif(
                File(ClassLoader.getSystemResource("images/tetris/Animation.gif").toURI()),
                maxFrames = 200
            ),
            fps = 2
        )
    }

    @Test
    fun testDirectoryTetris() {
        xled.showSequence(
            "Tetris", XledSequence.fromDirectory(
                File(ClassLoader.getSystemResource("images/tetris").toURI()),
                200
            ), 2
        )
    }

    @Test
    fun testDirectoryChristmas() {
        xled.showSequence(
            "Christmas", XledSequence.fromDirectory(
                File(ClassLoader.getSystemResource("images/christmas").toURI()),
                200
            ), 2
        )
    }

    @Test
    fun testDirectoryChristmasTree() {
        xled.showSequence(
            "Christmas", XledSequence.fromDirectory(
                File(ClassLoader.getSystemResource("images/christmas-tree").toURI()),
                200
            ), 2
        )
    }

    @Test
    fun testColor() {
        xled.color(RGBColor(0, 0, 0))
        xled.mode(DeviceMode.color)
        xled.deleteMovies()

        val fps = 1

        val sequence = XledSequence()

        var frame = XledFrame(xled.width, xled.height, RGBWColor(255, 255, 255, 128, normalize = false))
//        frame.setImage(File(ClassLoader.getSystemResource("images/tetris/teris-0.jpg").toURI()))
        sequence.add(frame)
//        frame = XledFrame(columns, rows, bytesPerLed, RGBColor(0, 0, 0))
//        frame.setImage(File(ClassLoader.getSystemResource("images/tetris/teris-1.jpg").toURI()))
//        sequence.add(frame)

//        val ins = ClassLoader.getSystemResourceAsStream("images/HelloWorld!.png")
//        val image = ImageIO.read(ins)
//        for (f in 0 until image.width - columns) {
//            val frame = XledFrame(columns, rows, bytesPerLed, RGBColor(0, 0, 0))
//            val imageFrame = image.getSubimage(f, 0, columns, rows)
//            frame.setImage(imageFrame)
//            sequence.add(frame)
//        }

//        File(ClassLoader.getSystemResource("images/tetris").toURI())
//            .listFiles { file -> file.name.lowercase().endsWith(".png") }
//            ?.forEach { file ->
//                println("### file: ${file.name}")
//                val frame = XledFrame(columns, rows, bytesPerLed)
//                frame.setImage(file)
//                sequence.add(frame)
//            }

        val numberOfFrames = sequence.size
        val newMovie = xled.newMovie(
            NewMovieRequest(
                name = "Foo",
                descriptorType = "rgbw_raw",
                ledsPerFrame = xled.deviceInfo.numberOfLed,
                framesNumber = numberOfFrames,
                fps = fps
            )
        )
        xled.uploadMovie(sequence.toByteArray(xled.bytesPerLed))
        xled.movieConfig(
            MovieConfig(
                frameDelay = 1000 / fps,
                ledsNumber = xled.deviceInfo.numberOfLed,
                framesNumber = numberOfFrames,
            )
        )
        xled.moviesCurrent(
            MoviesCurrentRequest(
                id = newMovie.id
            )
        )
        xled.mode(DeviceMode.movie)
    }
}
