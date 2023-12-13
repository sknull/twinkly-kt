package de.visualdigits.kotlin.twinkly.model.playable

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionDirection
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionType
import de.visualdigits.kotlin.util.FontUtil
import kotlinx.coroutines.delay
import org.apache.commons.lang3.math.NumberUtils.min
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.max

open class XledFrame(
    var width: Int = 0,
    var height: Int = 0,
    val initialColor: Color<*> = RGBColor(0, 0, 0),
    var frameDelay: Long = 1000,
    val frame: MutableList<MutableList<Color<*>>> = mutableListOf()
) : Playable, MutableList<MutableList<Color<*>>> by frame {

    private val log = LoggerFactory.getLogger(XledFrame::class.java)

    protected var running: Boolean = false

    constructor(
        bytes: ByteArray,
        initialColor: Color<*> = RGBColor(0, 0, 0),
        gamma: Double = 1.0
    ): this(ImageIO.read(bytes.inputStream()), initialColor, gamma)

    constructor(
        file: File,
        initialColor: Color<*> = RGBColor(0, 0, 0),
        gamma: Double = 1.0
    ): this(ImageIO.read(file), initialColor, gamma)

    constructor(
        ins: InputStream,
        initialColor: Color<*> = RGBColor(0, 0, 0),
        gamma: Double = 1.0
    ): this(ImageIO.read(ins), initialColor, gamma)

    constructor(
        image: BufferedImage,
        initialColor: Color<*> = RGBColor(0, 0, 0),
        gamma: Double = 1.0
    ): this(width = image.width, height = image.height, initialColor = initialColor) {
        setImage(image, gamma)
    }

    constructor(
        text: String,
        fontName: String,
        fontSize: Int,
        backgroundColor: Color<*>,
        textColor: Color<*>,
        gamma: Double = 1.0
    ) : this(
        image = FontUtil.drawText(
            text = text,
            fontName = fontName,
            fontSize = fontSize,
            backgroundColor = backgroundColor,
            textColor = textColor
        ),
        gamma = gamma
    )

    constructor(
        fontName: String,
        fontSize: Int,
        vararg texts: Triple<String, Color<*>, Color<*>>
    ): this() {
        val frames = texts.map { entry ->
            XledFrame(
                text = entry.first,
                fontName = fontName,
                fontSize = fontSize,
                backgroundColor = entry.second,
                textColor = entry.third
            )
        }

        val first = frames.first()
        initialize(width = first.width, height = first.height, initialColor = initialColor)
        replaceSubFrame(first, 0, 0)
        frames.drop(1).forEach { frame -> expandRight(frame) }
    }

    init {
        initialize(width, height, initialColor)
    }

    fun initialize(width: Int, height: Int, initialColor: Color<*> = RGBColor(0, 0, 0)) {
        this.width = width
        this.height = height
        for (x in 0 until width) {
            val column = mutableListOf<Color<*>>()
            for (y in 0 until height) {
                column.add(initialColor.clone())
            }
            frame.add(column)
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

    override fun play(
        xled: de.visualdigits.kotlin.twinkly.model.device.xled.XLed,
        loop: Int,
        random: Boolean,
        transitionType: TransitionType?,
        transitionDirection: TransitionDirection?,
        transitionBlendMode: BlendMode?,
        transitionDuration: Long,
        verbose: Boolean
    ) {
        if (verbose) log.info("\n$this")
        xled.mode(DeviceMode.rt)
        val n = max(1, frameDelay / 5000)
        var loopCount = loop
        while (loopCount == -1 || loopCount > 0) {
            for (j in 0 until n) {
                xled.showRealTimeFrame(this)
                if (loopCount != -1) loopCount--
                if (loopCount > 0) Thread.sleep(kotlin.math.min(5000, frameDelay))
            }
        }
    }

    override fun stop() {
        running = false
    }

    suspend fun fade(
        xled: de.visualdigits.kotlin.twinkly.model.device.xled.XLed,
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
        blendMode: BlendMode = BlendMode.REPLACE
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

    fun expandRight(
        numberOfColumns: Int,
        initialColor: Color<*> = RGBColor(0, 0, 0)
    ): XledFrame {
        for (n in 0 until numberOfColumns) {
            val column = mutableListOf<Color<*>>()
            for (y in 0 until height) {
                column.add(initialColor.clone())
            }
            frame.add(column)
        }
        width += numberOfColumns
        return this
    }

    fun expandLeft(
        numberOfColumns: Int,
        initialColor: Color<*> = RGBColor(0, 0, 0)
    ): XledFrame {
        for (n in 0 until numberOfColumns) {
            val column = mutableListOf<Color<*>>()
            for (y in 0 until height) {
                column.add(initialColor.clone())
            }
            frame.add(0, column)
        }
        width += numberOfColumns
        return this
    }

    fun expandTop(
        numberOfColumns: Int,
        initialColor: Color<*> = RGBColor(0, 0, 0)
    ): XledFrame {
        for (x in 0 until width) {
            for (n in 0 until numberOfColumns) {
                frame[x].add(0, initialColor.clone())
            }
        }
        height += numberOfColumns
        return this
    }

    fun expandBottom(
        numberOfColumns: Int,
        initialColor: Color<*> = RGBColor(0, 0, 0)
    ): XledFrame {
        for (x in 0 until width) {
            for (n in 0 until numberOfColumns) {
                frame[x].add(initialColor.clone())
            }
        }
        height += numberOfColumns
        return this
    }

    fun expandRight(
        frame: XledFrame,
    ): XledFrame {
        expandRight(frame.width)
        if (frame.height > height) {
            expandBottom(frame.height - height)
        }
        replaceSubFrame(frame, width - frame.width, 0)
        return this
    }

    fun expandLeft(
        frame: XledFrame,
    ): XledFrame {
        expandLeft(frame.width)
        if (frame.height > height) {
            expandBottom(frame.height - height)
        }
        replaceSubFrame(frame, 0, 0)
        return this
    }

    fun expandTop(
        frame: XledFrame,
    ): XledFrame {
        expandTop(frame.height)
        if (frame.width > width) {
            expandRight(frame.width - width)
        }
        replaceSubFrame(frame, 0, 0)
        return this
    }

    fun expandBottom(
        frame: XledFrame,
    ): XledFrame {
        expandBottom(frame.height)
        if (frame.width > width) {
            expandRight(frame.width - width)
        }
        replaceSubFrame(frame, 0, height - frame.height)
        return this
    }

    fun rotateRight(): XledFrame {
        val newFrame = XledFrame(height, width, initialColor, frameDelay)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[height - y - 1][x] = frame[x][y]
            }
        }
        return newFrame
    }

    fun rotateLeft(): XledFrame {
        val newFrame = XledFrame(height, width, initialColor, frameDelay)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[y][width - x - 1] = frame[x][y]
            }
        }
        return newFrame
    }

    fun rotate180(): XledFrame {
        val newFrame = XledFrame(width, height, initialColor, frameDelay)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[width - x - 1][height - y - 1] = frame[x][y]
            }
        }
        return newFrame
    }

    fun drawLine(x0: Int, y0: Int, x1: Int, y1: Int, color: Color<*>): XledFrame {
        // see also here: https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
        if (abs(y1 - y0) < abs(x1 - x0)) {
            if (x0 > x1) {
                drawLineLow(x1, y1, x0, y0, color)
            } else {
                drawLineLow(x0, y0, x1, y1, color)
            }
        } else {
            if (y0 > y1) {
                drawLineHigh(x1, y1, x0, y0, color)
            } else {
                drawLineHigh(x0, y0, x1, y1, color)
            }
        }
        return this
    }

    private fun drawLineLow(x0: Int, y0: Int, x1: Int, y1: Int, color: Color<*>) {
        val dx = x1 - x0
        var dy = y1 - y0
        var yi = 1
        if (dy < 0) {
            yi = -1
            dy = -dy
        }
        var D = 2 * dy - dx
        var y = y0
        for (x in x0 .. x1) {
            frame[x][y] = color.clone()
            if (D > 0) {
                y += yi
                D += 2 * (dy - dx)
            } else {
                D += 2 * dy
            }
        }
    }

    private fun drawLineHigh(x0: Int, y0: Int, x1: Int, y1: Int, color: Color<*>) {
        var dx = x1 - x0
        val dy = y1 - y0
        var xi = 1
        if (dx < 0) {
            xi = -1
            dx = -dx
        }
        var D = 2 * dx - dy
        var x = x0
        for (y in y0 .. y1) {
            frame[x][y] = color.clone()
            if (D > 0) {
                x += xi
                D += 2 * (dx - dy)
            } else {
                D += 2 * dx
            }
        }
    }

    fun fade(other: XledFrame, factor: Double, blendMode: BlendMode = BlendMode.AVERAGE): XledFrame {
        val newFrame = XledFrame(width, height, initialColor, frameDelay)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[x][y] = frame[x][y].fade(other[x][y], factor, blendMode)
            }
        }
        return newFrame
    }

    fun setImage(file: File): XledFrame {
        return setImage(ImageIO.read(file))
    }

    fun setImage(ins: InputStream): XledFrame {
        return setImage(ImageIO.read(ins))
    }

    fun setImage(
        image: BufferedImage,
        gamma: Double = 1.0
    ): XledFrame {

        for (y in 0 until height) {
            for (x in 0 until width) {
                val colors = frame[x]
                colors[y] = RGBColor(image.getRGB(x, y).toLong())
            }
        }
        return this
    }

    override fun toByteArray(bytesPerLed: Int): ByteArray {
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

    override fun frames(): List<Playable> = listOf(this)
}
