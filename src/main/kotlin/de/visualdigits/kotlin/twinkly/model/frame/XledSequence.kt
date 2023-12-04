package de.visualdigits.kotlin.twinkly.model.frame

import com.madgag.gif.fmsware.GifDecoder
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.IllegalArgumentException
import kotlin.math.min

class XledSequence(
    private val sequence: MutableList<XledFrame> = mutableListOf()
) : MutableList<XledFrame> by sequence {

    protected var running: Boolean = false

    suspend fun play(
        xled: XLed,
        frameDelay: Long,
        loop: Boolean = true,
        random: Boolean = false
    ) {
        val currentMode = xled.mode()
        xled.mode(DeviceMode.rt)
        running = true
        while (running) {
            if (random) {
                xled.showRealTimeFrame(sequence.random())
                delay(frameDelay)
            } else {
                sequence.forEach { frame ->
                    xled.showRealTimeFrame(frame)
                    delay(frameDelay)
                }
            }
            running = loop
        }
        xled.mode(currentMode)
    }

    fun stop() {
        running = false
    }

    fun toByteArray(bytesPerLed: Int): ByteArray {
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
            val files = directory.listFiles { file -> file.isFile && file.name.lowercase().endsWith(".png") }
            files?.sort()
            files?.take(maxFrames)
                ?.forEach { file ->
                    sequence.add(XledFrame.fromImage(file, initialColor))
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
