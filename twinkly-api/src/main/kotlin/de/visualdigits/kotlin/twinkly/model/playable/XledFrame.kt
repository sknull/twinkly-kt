package de.visualdigits.kotlin.twinkly.model.playable

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.Rotation
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.LedMode
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionDirection
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionType
import de.visualdigits.kotlin.util.FontUtil
import de.visualdigits.kotlin.util.SystemUtils
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin


open class XledFrame(
    var width: Int = 0,
    var height: Int = 0,
    val initialColor: Color<*> = RGBColor(0, 0, 0),
    var frameDelay: Long = 1000,
    var rotation: Rotation = Rotation.NONE
) : Playable {

    private val log = LoggerFactory.getLogger(XledFrame::class.java)

    override var running: Boolean = false

    private var frame: Array<Color<*>> = Array(width * height) { initialColor }
    
    constructor(
        bytes: ByteArray,
        initialColor: Color<*> = RGBColor(0, 0, 0),
        rotation: Rotation = Rotation.NONE
    ): this(ImageIO.read(bytes.inputStream()), initialColor, rotation = rotation)

    constructor(
        file: File,
        initialColor: Color<*> = RGBColor(0, 0, 0),
        rotation: Rotation = Rotation.NONE
    ): this(ImageIO.read(file), initialColor, rotation = rotation)

    constructor(
        ins: InputStream,
        initialColor: Color<*> = RGBColor(0, 0, 0),
        rotation: Rotation = Rotation.NONE
    ): this(ImageIO.read(ins), initialColor, rotation = rotation)

    constructor(
        image: BufferedImage,
        initialColor: Color<*> = RGBColor(0, 0, 0),
        rotation: Rotation = Rotation.NONE
    ): this(
        width = image.width,
        height = image.height,
        initialColor = initialColor,
        rotation = rotation
    ) {
        frame = getImageRaster(image, initialColor)
    }

    /**
     * Constructs a frame from the given text with the width and height of the resulting image.
     * The frame will be probably wider than a xled device and is meant for scrolling.
     */
    constructor(
        text: String,
        fontDirectory: File? = if (SystemUtils.IS_OS_WINDOWS) File("c:/Windows/Fonts") else null,
        fontName: String,
        fontSize: Int,
        backgroundColor: Color<*> = RGBColor(0, 0, 0),
        textColor: Color<*> = RGBColor(255, 0, 0),
        rotation: Rotation = Rotation.NONE
    ) : this(
        image = FontUtil.drawText(
            text = text,
            fontDirectory = fontDirectory,
            fontName = fontName,
            fontSize = fontSize,
            backgroundColor = backgroundColor,
            textColor = textColor
        ),
        rotation = rotation
    )

    /**
     * Constructs a frame from the given texts with the width and height of the resulting image.
     * The frame will be probably wider than a xled device and is meant for scrolling.
     */
    constructor(
        fontDirectory: File? = if (SystemUtils.IS_OS_WINDOWS) File("c:/Windows/Fonts") else null,
        fontName: String,
        fontSize: Int,
        texts: List<Triple<String, Color<*>, Color<*>>>,
        rotation: Rotation = Rotation.NONE
    ): this() {
        join(texts.map { entry ->
            XledFrame(
                text = entry.first,
                fontDirectory = fontDirectory,
                fontName = fontName,
                fontSize = fontSize,
                backgroundColor = entry.second,
                textColor = entry.third,
                rotation = rotation
            )
        })
    }

    /**
     * Constructs a frame from the given text with the width and height of the resulting image.
     * The frame will be probably wider than a xled device and is meant for scrolling.
     */
    constructor(
        text: String,
        fontName: String = "6x10",
        backgroundColor: Color<*> = RGBColor(0, 0, 0),
        textColor: Color<*> = RGBColor(255, 0, 0),
        rotation: Rotation = Rotation.NONE
    ) : this() {
        val figletFrame = FontUtil.drawFigletText(
            text = text,
            fontName = fontName,
            backgroundColor = backgroundColor,
            textColor = textColor
        )
        this.rotation = rotation
        this.frame = figletFrame.frame
        this.width = figletFrame.width
        this.height = figletFrame.height
    }

    /**
     * Constructs a frame from the given texts with the width and height of the resulting image.
     * The frame will be probably wider than a xled device and is meant for scrolling.
     */
    constructor(
        fontName: String = "6x10",
        texts: List<Triple<String, Color<*>, Color<*>>>,
        rotation: Rotation = Rotation.NONE
    ): this() {
        join(texts.map { entry ->
            XledFrame(
                text = entry.first,
                fontName = fontName,
                backgroundColor = entry.second,
                textColor = entry.third,
                rotation = rotation
            )
        })
    }

    companion object {

        fun getImageRaster(image: BufferedImage, initialColor: Color<*>): Array<Color<*>> {
            val frame: Array<Color<*>> = Array(image.width * image.height) { initialColor }
            val hasAlphaChannel = image.alphaRaster != null
            (image.raster.getDataBuffer() as DataBufferByte)
                .getData()
                .asSequence()
                .chunked(if (hasAlphaChannel) 4 else 3) // create chunks for each pixel
                .forEachIndexed { index, chunk ->
                    var (i, argb) = if (hasAlphaChannel) {
                        Pair(1, ((chunk[0].toInt() and 0xff) shl 24))
                    } else {
                        Pair(0, -16777216)
                    }
                    argb += (chunk[i++].toInt() and 0xff) // blue value
                    argb += ((chunk[i++].toInt() and 0xff) shl 8) // green value
                    argb += ((chunk[i].toInt() and 0xff) shl 16) // red value

                    frame[index] = RGBColor(argb.toLong())
                }

            return frame
        }
    }

    fun toBufferedImage(): BufferedImage {
        val image = BufferedImage(width,  height, BufferedImage.TYPE_INT_RGB)
        val dataBuffer = image.raster.getDataBuffer() as DataBufferInt
        frame.forEachIndexed { index, color ->
            dataBuffer.setElem(index, color.toRgbColor().value().toInt())
        }

        return image
    }

    override fun toByteArray(bytesPerLed: Int): ByteArray {
        val bytes = ByteArray(width * height * bytesPerLed) { 0 }
        var index = 0
        // !! attention - twinkly byte array is organized in a sequence of columns - not rows !!
        for (x in 0 until width) {
            for (y in 0 until height) {
                toLedPixel(this[x, y], bytesPerLed).copyInto(bytes, index)
                index += bytesPerLed
            }
        }
        return bytes
    }

    private fun toLedPixel(color: Color<*>, bytesPerLed: Int): ByteArray {
        return if (bytesPerLed == 4) {
            when (color) {
                is RGBColor -> color.toRgbwColor()
                is HSVColor -> color.toRgbwColor()
                is RGBWColor -> color
                else -> null
            }?.let { c ->
                byteArrayOf(
                    c.white.toByte(),
                    c.red.toByte(),
                    c.green.toByte(),
                    c.blue.toByte()
                )
            }?:byteArrayOf()
        } else {
            when (color) {
                is RGBColor -> color
                is HSVColor -> color.toRgbColor()
                is RGBWColor -> color.toRgbColor()
                else -> null
            }?.let { c ->
                byteArrayOf(
                    c.red.toByte(),
                    c.green.toByte(),
                    c.blue.toByte()
                )
            }?:byteArrayOf()
        }
    }

    private fun XledFrame.join(frames: List<XledFrame>) {
        val first = frames.first()
        initialize(width = first.width, height = first.height, initialColor = initialColor)
        replaceSubFrame(first, 0, 0)
        frames.drop(1).forEach { frame -> expandRight(frame) }
    }

    private fun initialize(width: Int, height: Int, initialColor: Color<*> = RGBColor(0, 0, 0)) {
        this.width = width
        this.height = height
        this.frame = Array(width * height) { initialColor }
    }

    operator fun set(x: Int, y: Int, color: Color<*>) {
        if (x in 0..<width && y >= 0 && y < height) {
            frame[width * y + x] = color
        }
    }

    operator fun get(x: Int, y: Int): Color<*> {
        return if (x in 0..<width && y >= 0 && y < height) {
            frame[width * y + x]
        } else {
            RGBColor(0, 0, 0)
        }
    }

    override fun toString(): String {
        return frame
            .asSequence()
            .chunked(width)
            .joinToString("\n") { row ->
                row.joinToString("") { color ->
                    color.ansiColor().repeat(2)
                }
            }
    }

    fun clone(): XledFrame {
        val clone = XledFrame(width, height, initialColor)
        for (y in 0 until height) {
            for (x in 0 until width) {
                clone[x, y] = this[x, y].clone()
            }
        }
        return clone
    }

    fun setColor(color: Color<*>) {
        frame.forEachIndexed { index, _ ->
            frame[index] = color.clone()
        }
    }

    override fun play(
        xled: XLed,
        loop: Int,
        transitionType: TransitionType,
        randomSequence: Boolean,
        transitionDirection: TransitionDirection,
        transitionBlendMode: BlendMode,
        transitionDuration: Long,
        verbose: Boolean
    ) {
        if (verbose) log.info("\n$this")

        xled.setLedMode(LedMode.rt)
        val rotated = when (rotation) {
            Rotation.LEFT -> rotateLeft()
            Rotation.RIGHT -> rotateRight()
            Rotation.FULL -> rotate180()
            else -> this
        }

        val n = max(1, frameDelay / 5000)
        var loopCount = loop
        var t = System.currentTimeMillis()
        while (loopCount == -1 || loopCount > 0) {
            (0 until n).forEach { _ ->
                xled.showRealTimeFrame(rotated)
                if (loopCount != -1) loopCount--
                if (loopCount > 0) Thread.sleep(min(5000, frameDelay))
            }
            val now = System.currentTimeMillis()
            if (t - now > 5000) {
                t = now
                xled.setLedMode(LedMode.rt)
            }
        }
    }

    suspend fun fade(
        xled: XLedDevice,
        color: Color<*>,
        millis: Long
    ) {
        val delay = millis / 255
        val oldFrame = clone()
        for (f in 0 until 256) {
            xled.showRealTimeFrame(this)
            delay(max(0, delay))
            oldFrame.frame.forEachIndexed { index, oldColor ->
                frame[index] = oldColor.fade(color, f / 256.0, BlendMode.AVERAGE)
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
                subFrame[x, y] = this[x + offsetX, y + offsetY].clone()
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
                this[x + offsetX, y + offsetY] = this[x + offsetX, y + offsetY].blend(subFrame[x, y], blendMode)
            }
        }

        return this
    }

    fun expandRight(
        numberOfColumns: Int,
        initialColor: Color<*> = RGBColor(0, 0, 0)
    ): XledFrame {
        val newFrame = XledFrame(width + numberOfColumns, height, initialColor)
        newFrame.replaceSubFrame(this, 0, 0)
        width += numberOfColumns
        frame = newFrame.frame

        return this
    }

    fun expandLeft(
        numberOfColumns: Int,
        initialColor: Color<*> = RGBColor(0, 0, 0)
    ): XledFrame {
        val newFrame = XledFrame(width + numberOfColumns, height, initialColor)
        newFrame.replaceSubFrame(this, numberOfColumns, 0)
        width += numberOfColumns
        frame = newFrame.frame

        return this
    }

    fun expandTop(
        numberOfColumns: Int,
        initialColor: Color<*> = RGBColor(0, 0, 0)
    ): XledFrame {
        val newFrame = XledFrame(width, height + numberOfColumns, initialColor)
        newFrame.replaceSubFrame(this, 0, numberOfColumns)
        height += numberOfColumns
        frame = newFrame.frame

        return this
    }

    fun expandBottom(
        numberOfColumns: Int,
        initialColor: Color<*> = RGBColor(0, 0, 0)
    ): XledFrame {
        val newFrame = XledFrame(width, height + numberOfColumns, initialColor)
        newFrame.replaceSubFrame(this, 0, 0)
        height += numberOfColumns
        frame = newFrame.frame

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

    open fun rotateRight(): XledFrame {
        val newFrame = XledFrame(height, width, initialColor, frameDelay)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[height - y - 1, x] = this[x, y]
            }
        }

        return newFrame
    }

    open fun rotateLeft(): XledFrame {
        val newFrame = XledFrame(height, width, initialColor, frameDelay)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[y, width - x - 1] = this[x, y]
            }
        }

        return newFrame
    }

    open fun rotate180(): XledFrame {
        val newFrame = XledFrame(width, height, initialColor, frameDelay)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[width - x - 1, height - y - 1] = this[x, y]
            }
        }

        return newFrame
    }

    fun drawCircle(cx: Int, cy: Int, rx: Int, ry: Int, color: Color<*>): XledFrame {
        for (a in 0 until 360) {
            val x = (cx + rx * cos(a * PI / 180.0)).roundToInt()
            val y = (cy + ry * sin(a * PI / 180.0)).roundToInt()
            this[x, y] = color
        }

        return this
    }

    fun drawRect(x0: Int, y0: Int, x1: Int, y1: Int, color: Color<*>): XledFrame {
        drawLine(x0, y0, x1, y0, color)
        drawLine(x1, y0, x1, y1, color)
        drawLine(x1, y1, x0, y1, color)
        drawLine(x0, y1, x0, y0, color)

        return this
    }

    fun fillRect(x0: Int, y0: Int, x1: Int, y1: Int, color: Color<*>): XledFrame {
        for (x in x0 .. x1) {
            drawLine(x, y1, x, y0, color)
        }

        return this
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
        var d = 2 * dy - dx
        var y = y0
        for (x in x0 .. x1) {
            this[x, y] = color.clone()
            if (d > 0) {
                y += yi
                d += 2 * (dy - dx)
            } else {
                d += 2 * dy
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
        var d = 2 * dx - dy
        var x = x0
        for (y in y0 .. y1) {
            this[x, y] = color.clone()
            if (d > 0) {
                x += xi
                d += 2 * (dx - dy)
            } else {
                d += 2 * dx
            }
        }
    }

    fun fade(other: XledFrame, factor: Double, blendMode: BlendMode = BlendMode.AVERAGE): XledFrame {
        val newFrame = XledFrame(width, height, initialColor, frameDelay)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[x, y] = this[x, y].fade(other[x, y], factor, blendMode)
            }
        }

        return newFrame
    }

    override fun frames(): List<Playable> = listOf(this)
}
