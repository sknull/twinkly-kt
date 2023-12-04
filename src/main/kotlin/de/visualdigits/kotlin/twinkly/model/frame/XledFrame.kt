package de.visualdigits.kotlin.twinkly.model.frame

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import kotlinx.coroutines.delay
import org.apache.commons.lang3.math.NumberUtils.min
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.lang.StringBuilder
import javax.imageio.ImageIO
import kotlin.math.max

open class XledFrame(
    val width: Int,
    val height: Int,
    val initialColor: Color<*> = RGBColor(0, 0, 0),
    val frame: MutableList<MutableList<Color<*>>> = mutableListOf()
) : MutableList<MutableList<Color<*>>> by frame {

    protected var running: Boolean = false

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
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledFrame {
            return fromImage(ImageIO.read(file), initialColor)
        }

        fun fromImage(
            ins: InputStream,
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledFrame {
            return fromImage(ImageIO.read(ins), initialColor)
        }

        fun fromImage(
            image: BufferedImage,
            initialColor: Color<*> = RGBColor(0, 0, 0)
        ): XledFrame {
            return XledFrame(image.width, image.height, initialColor).setImage(image)
        }
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = frame[x][y].ansiColor()
                sb.append(pixel).append(pixel)
            }
            sb.append('\n')
        }
        return sb.toString()
    }

    fun clone(): XledFrame {
        val clone = XledFrame(width, height, initialColor)
        for (y in 0 until height) {
            for (x in 0 until width) {
                clone[x][y] = frame[x][y].clone()
            }
        }
        return clone
    }

    fun setColor(color: Color<*>) {
        for (y in 0 until height) {
            for (x in 0 until width) {
                frame[x][y] = color.clone()
            }
        }
    }

    suspend fun play(
        xled: XLed,
        frameDelay: Long = 1000
    ) {
        val currentMode = xled.mode()
        xled.mode(DeviceMode.rt)
        running = true
        while (running) {
            xled.showRealTimeFrame(this)
            delay(frameDelay)
        }
        xled.mode(currentMode)
    }

    fun stop() {
        running = false
    }

    suspend fun fade(
        xled: XLed,
        color: Color<*>,
        millis: Long
    ) {
        val delay = millis / 255
        val oldFrame = clone()
        for (f in 0 until 256) {
            xled.showRealTimeFrame(this)
            delay(max(0, delay))
            for (y in 0 until height) {
                for (x in 0 until width) {
                    frame[x][y] = oldFrame[x][y].fade(color, f / 256.0, BlendMode.AVERAGE)
                }
            }
        }
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
        offsetY: Int = 0,
        blendMode: BlendMode = BlendMode.AVERAGE
    ): XledFrame {
        val xStart = if (offsetX >= 0) 0 else -1 * offsetX
        val yStart = if (offsetY >= 0) 0 else -1 * offsetY

        for (y in yStart until min(subFrame.height, height - offsetY)) {
            for (x in xStart until min(subFrame.width, width - offsetX)) {
                frame[x + offsetX][y + offsetY] = frame[x + offsetX][y + offsetY].blend(subFrame[x][y], blendMode)
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
                colors[y] = RGBColor(image.getRGB(x, y).toLong())
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
