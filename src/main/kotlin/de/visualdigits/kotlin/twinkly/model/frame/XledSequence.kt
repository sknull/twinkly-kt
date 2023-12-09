package de.visualdigits.kotlin.twinkly.model.frame

import com.madgag.gif.fmsware.GifDecoder
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

class XledSequence(
    private val sequence: MutableList<Playable> = mutableListOf()
) : Playable, MutableList<Playable> by sequence {

    private val log = LoggerFactory.getLogger(XledSequence::class.java)

    private var running: Boolean = false

    override fun play(
        xled: XLed,
        frameDelay: Long,
        sequenceDelay: Long,
        frameLoop: Int,
        random: Boolean
    ) {
        val n = max(1, frameDelay / 5000)
        var frameLoopCount = frameLoop
        while (frameLoopCount == -1 || frameLoopCount-- > 0) {
            if (random) {
                val playable = sequence.random()
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
                            frameDelay = sequenceDelay,
                            loop = (frameDelay / sequenceDelay / playable.size).toInt()
                        )
                        if (frameLoopCount != -1) frameLoopCount--
                    }
                }
            } else {
                sequence.forEach { playable ->
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
                                frameDelay = sequenceDelay,
                                loop = (frameDelay / sequenceDelay / playable.size).toInt()
                            )
                        }
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
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledSequence {
            if (!directory.isDirectory) throw IllegalArgumentException("Given file is not a directory")
            val sequence = XledSequence()
            val files = directory.listFiles { file -> file.isDirectory || (file.isFile && file.name.lowercase().endsWith(".png")) }
            files?.sortBy { it.name.lowercase() }
            files?.take(maxFrames)
                ?.forEach { file ->
                    if (file.isFile) {
                        sequence.add(XledFrame.fromImage(file, initialColor))
                    } else {
                        sequence.add(XledSequence.fromDirectory(file, maxFrames, initialColor))
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
