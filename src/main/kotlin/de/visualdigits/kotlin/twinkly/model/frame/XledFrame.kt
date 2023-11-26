package de.visualdigits.kotlin.twinkly.model.frame

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import org.apache.commons.lang3.math.NumberUtils.min
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

class XledFrame(
    val width: Int,
    val height: Int,
    val initialColor: Color<*> = RGBColor(0, 0, 0),
    private val frame: MutableList<MutableList<Color<*>>> = mutableListOf()
) : MutableList<MutableList<Color<*>>> by frame {

    init {
        for (x in 0 until width) {
            val row = mutableListOf<Color<*>>()
            for (y in 0 until height) {
                row.add(initialColor.clone())
            }
            frame.add(row)
        }
    }

    companion object {

        fun fromImage(
            file: File,
            initialColor: Color<*> = RGBColor(0, 0, 0, false)
        ): XledFrame {
            return fromImage(ImageIO.read(file), initialColor)
        }

        fun fromImage(
            ins: InputStream,
            initialColor: Color<*> = RGBColor(0, 0, 0, false)
        ): XledFrame {
            return fromImage(ImageIO.read(ins), initialColor)
        }

        fun fromImage(
            image: BufferedImage,
            initialColor: Color<*> = RGBColor(0, 0, 0, false)
        ): XledFrame {
            return XledFrame(image.width, image.height, initialColor).setImage(image)
        }

//        fun fromTest(
//            text: String,
//            size: Int,
//            fontName: String,
//            color: java.awt.Color,
//        ): XledFrame {
//
//        }
    }

    /**
     * Returns a new XledFrame instance which contains all pixels in the given area.
     */
    fun subFrame(
        offsetX: Int = 0,
        offsetY: Int = 0,
        width: Int = this.width,
        height: Int = this.height
    ): XledFrame {
        val subFrame = XledFrame(width, height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                subFrame[x][y] = frame[x + offsetX][y + offsetY].clone()
            }
        }
        return subFrame
    }

    fun replaceSubFrame(
        subFrame: XledFrame,
        offsetX: Int = 0,
        offsetY: Int = 0
    ): XledFrame {
        val xStart = if (offsetX >= 0) 0 else -1 * offsetX
        val yStart = if (offsetY >= 0) 0 else -1 * offsetY

        for (y in yStart until min(subFrame.height, height - offsetY)) {
            for (x in xStart until min(subFrame.width, width - offsetX)) {
                frame[x + offsetX][y + offsetY] = subFrame[x][y].clone()
            }
        }
        return this
    }

    fun setImage(file: File): XledFrame {
        return setImage(ImageIO.read(file))
    }

    fun setImage(ins: InputStream): XledFrame {
        return setImage(ImageIO.read(ins))
    }

    fun setImage(image: BufferedImage): XledFrame {
        for (y in 0 until height) {
            for (x in 0 until width) {
                val colors = frame[x]
                colors[y] = RGBColor(image.getRGB(x, y).toLong(), false)
            }
        }
        return this
    }

    fun toByteArray(bytesPerLed: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        for (x in 0 until width) {
            for (y in 0 until height) {
                baos.write(createPixel(frame[x][y], bytesPerLed))
            }
        }
        return baos.toByteArray()
    }

    private fun createPixel(color: Color<*>, bytesPerLed: Int): ByteArray {
        return when (color) {
            is RGBWColor -> {
                if (bytesPerLed == 4) {
                    byteArrayOf(
                        color.white.toByte(),
                        color.red.toByte(),
                        color.green.toByte(),
                        color.blue.toByte()
                    )
                }
                else {
                    val rgbColor = color.toRGB()
                    byteArrayOf(
                        rgbColor.red.toByte(),
                        rgbColor.green.toByte(),
                        rgbColor.blue.toByte()
                    )
                }
            }
            is RGBColor -> {
                if (bytesPerLed == 4) {
                    val rgbwColor = color.toRGBW()
                    byteArrayOf(
                        rgbwColor.white.toByte(),
                        rgbwColor.red.toByte(),
                        rgbwColor.green.toByte(),
                        rgbwColor.blue.toByte()
                    )
                }
                else {
                    byteArrayOf(
                        color.red.toByte(),
                        color.green.toByte(),
                        color.blue.toByte()
                    )
                }
            }
            else -> byteArrayOf()
        }
    }
}
