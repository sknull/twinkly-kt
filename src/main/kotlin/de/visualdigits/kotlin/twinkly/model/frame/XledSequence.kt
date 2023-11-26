package de.visualdigits.kotlin.twinkly.model.frame

import com.madgag.gif.fmsware.GifDecoder
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.IllegalArgumentException
import kotlin.math.min

class XledSequence(
    val sequence: MutableList<XledFrame> = mutableListOf()
) : MutableList<XledFrame> by sequence {

    fun toByteArray(bytesPerLed: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        forEach { baos.write(it.toByteArray(bytesPerLed)) }
        return baos.toByteArray()
    }

    companion object {
        fun fromDirectory(
            columns: Int,
            rows: Int,
            bytesPerLed: Int,
            directory: File,
            maxFrames: Int = Int.MAX_VALUE,
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledSequence {
            if (!directory.isDirectory) throw IllegalArgumentException("Given file is not a directory")
            val sequence = XledSequence()
            directory.listFiles { file -> file.isFile && file.name.lowercase().endsWith(".png") }
                ?.take(maxFrames)
                ?.forEach { file ->
                    sequence.add(XledFrame.fromImage(columns, rows, file, initialColor))
                }
            return sequence
        }

        fun fromAnimatedGif(
            columns: Int,
            rows: Int,
            bytesPerLed: Int,
            file: File,
            maxFrames: Int = Int.MAX_VALUE,
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledSequence {
            return fromAnimatedGif(columns, rows, bytesPerLed, FileInputStream(file), maxFrames, initialColor)
        }

        fun fromAnimatedGif(
            columns: Int,
            rows: Int,
            bytesPerLed: Int,
            ins: InputStream,
            maxFrames: Int = Int.MAX_VALUE,
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledSequence {
            val sequence = XledSequence()
            val gifDecoder = GifDecoder()
            gifDecoder.read(ins)
            for (f in 0 until min(gifDecoder.frameCount, maxFrames)) {
                sequence.add(
                    XledFrame.fromImage(columns, rows, gifDecoder.getFrame(f), initialColor)
                )
            }
            return sequence
        }
    }
}
