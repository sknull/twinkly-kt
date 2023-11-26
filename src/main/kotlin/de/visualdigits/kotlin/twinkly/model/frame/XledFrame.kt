package de.visualdigits.kotlin.twinkly.model.frame

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import org.apache.commons.lang3.math.NumberUtils.max
import org.apache.commons.lang3.math.NumberUtils.min
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

class XledFrame(
    val columns: Int,
    val rows: Int,
    val initialColor: Color<*> = RGBColor(0, 0, 0),
    private val frame: MutableList<MutableList<Color<*>>> = mutableListOf()
) : MutableList<MutableList<Color<*>>> by frame {

    init {
        for (x in 0 until columns) {
            val row = mutableListOf<Color<*>>()
            for (y in 0 until rows) {
                row.add(initialColor.clone())
            }
            frame.add(row)
        }
    }

    companion object {

        fun fromImage(
            columns: Int,
            rows: Int,
            bytesPerLed: Int,
            file: File,
            initialColor: Color<*> = RGBColor(0, 0, 0, false)
        ): XledFrame {
            return fromImage(columns, rows, bytesPerLed, ImageIO.read(file), initialColor)
        }

        fun fromImage(
            columns: Int,
            rows: Int,
            bytesPerLed: Int,
            ins: InputStream,
            initialColor: Color<*> = RGBColor(0, 0, 0, false)
        ): XledFrame {
            return fromImage(columns, rows, bytesPerLed, ImageIO.read(ins), initialColor)
        }

        fun fromImage(
            columns: Int,
            rows: Int,
            bytesPerLed: Int,
            image: BufferedImage,
            initialColor: Color<*> = RGBColor(0, 0, 0, false)
        ): XledFrame {
            return XledFrame(columns, rows, initialColor).setImage(image)
        }
    }

    /**
     * Returns a new XledFrame instance which contains all pixels in the given area.
     */
    fun subFrame(
        offsetX: Int = 0,
        offsetY: Int = 0,
        width: Int = columns,
        height: Int = rows
    ): XledFrame {
        val subFrame = XledFrame(width, height, initialColor)
        for (y in offsetY until height) {
            for (x in offsetX until width) {
                val current = frame[x][y]
                subFrame[max(x - offsetX, 0)][max(y - offsetY, 0)] = when (current) {
                    is RGBColor -> RGBColor(current.red, current.green, current.blue)
                    is RGBWColor -> RGBWColor(current.red, current.green, current.blue, current.white)
                    else -> {
                        val rgbCurrent = current.toRGB()
                        RGBColor(rgbCurrent.red, rgbCurrent.green, rgbCurrent.blue)
                    }
                }
            }
        }
        return subFrame
    }

    fun replaceSubFrame(
        subFrame: XledFrame,
        offsetX: Int = 0,
        offsetY: Int = 0
    ): XledFrame {
        for (y in 0 until subFrame.rows) {
            for (x in 0 until subFrame.columns) {
                val current = subFrame[x][y]
                frame[min(x + offsetX, columns - 1)][min(y + offsetY, rows - 1)] = when (current) {
                    is RGBColor -> RGBColor(current.red, current.green, current.blue)
                    is RGBWColor -> RGBWColor(current.red, current.green, current.blue, current.white)
                    else -> {
                        val rgbCurrent = current.toRGB()
                        RGBColor(rgbCurrent.red, rgbCurrent.green, rgbCurrent.blue)
                    }
                }
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
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                val colors = frame[x]
                colors[y] = RGBColor(image.getRGB(x, y).toLong(), false)
            }
        }
        return this
    }

    fun toByteArray(bytesPerLed: Int): ByteArray {
        val baos = ByteArrayOutputStream()
        for (x in 0 until columns) {
            for (y in 0 until rows) {
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
