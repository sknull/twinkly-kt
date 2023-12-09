package de.visualdigits.kotlin.twinkly.model.frame

import com.madgag.gif.fmsware.GifDecoder
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.scene.Scene
import de.visualdigits.kotlin.twinkly.model.scene.SceneType
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

class XledSequence(
    private val sequence: MutableList<Playable> = mutableListOf(),
    var frameDelay: Long = 100
) : Playable, MutableList<Playable> by sequence {

    private val log = LoggerFactory.getLogger(XledSequence::class.java)

    private var running: Boolean = false

    override fun play(
        xled: XLed,
        loop: Int,
        random: Boolean
    ) {
        val n = max(1, frameDelay / 5000)
        var frameLoopCount = loop
        while (frameLoopCount == -1 || frameLoopCount-- > 0) {
            for (j in 0 until sequence.size) {
                val playable = if (random) sequence.random() else sequence[j]
                log.info("\n$playable")
                when (playable) {
                    is XledFrame -> {
                        for (i in 0 until n) {
                            xled.showRealTimeFrame(playable)
                            Thread.sleep(min(5000, frameDelay))
                        }
                    }
                    is XledSequence -> {
                        xled.showRealTimeSequence(
                            frameSequence = playable,
                            loop = (frameDelay / playable.frameDelay / playable.size).toInt()
                        )
                    }
                }
            }
            if (frameLoopCount != -1) frameLoopCount--
            if (frameLoopCount > 0) Thread.sleep(5000)
        }
    }

    override fun toString(): String {
        val frames =  sequence
            .map { it.toString().split("\n") }
        val sb = StringBuilder()
        for (y in 0 until frames[0].size) {
            sb.append(frames.map { it[y] }.joinToString(" ") + "\n")
        }
        return sb.toString()
    }

    override fun stop() {
        running = false
    }

    override fun toByteArray(bytesPerLed: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        forEach { baos.write(it.toByteArray(bytesPerLed)) }
        return baos.toByteArray()
    }

    companion object {
        fun fromDirectory(
            directory: File,
            maxFrames: Int = Int.MAX_VALUE,
            initialColor: Color<*> = RGBColor(0, 0, 0),
            frameDelay: Long = 1000
        ): XledSequence {
            if (!directory.isDirectory) throw IllegalArgumentException("Given file is not a directory")
            val sequence = XledSequence(frameDelay = frameDelay)
            directory
                .listFiles { file -> file.isDirectory }
                ?.sortedBy { it.name.lowercase() }
                ?.take(maxFrames)
                ?.forEach { sceneDirectory ->
                    val sceneFile = File(sceneDirectory, "scene.json")
                    if (sceneFile.exists()) {
                        val scene = Scene.unmarshall(sceneFile)
                        val images = sceneDirectory.listFiles { file -> file.isFile && file.name.lowercase().endsWith(".png") }
                        if (images != null) {
                            when (scene.type) {
                                SceneType.frame -> {
                                    sequence.add(XledFrame.fromImage(images.first(), initialColor))
                                }
                                SceneType.sequence -> {
                                    val subSequence = XledSequence()
                                    scene.frameDelay?.let { subSequence.frameDelay = it }
                                    images.forEach { image -> subSequence.add(XledFrame.fromImage(image, initialColor)) }
                                    sequence.add(subSequence)
                                }
                            }
                        }
                    }
                }
            return sequence
        }

        fun fromAnimatedGif(
            file: File,
            maxFrames: Int = Int.MAX_VALUE,
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledSequence {
            return fromAnimatedGif(FileInputStream(file), maxFrames, initialColor)
        }

        fun fromAnimatedGif(
            ins: InputStream,
            maxFrames: Int = Int.MAX_VALUE,
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledSequence {
            val sequence = XledSequence()
            val gifDecoder = GifDecoder()
            gifDecoder.read(ins)
            for (f in 0 until min(gifDecoder.frameCount, maxFrames)) {
                sequence.add(
                    XledFrame.fromImage(gifDecoder.getFrame(f), initialColor)
                )
            }
            return sequence
        }
    }
}
