package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.font.Direction
import de.visualdigits.kotlin.twinkly.model.font.FigletFont
import de.visualdigits.kotlin.twinkly.model.font.FigletSmusher
import de.visualdigits.kotlin.twinkly.model.font.Justify
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import java.awt.Font
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths


object FontUtil {

    private var iterator = 0
    private var maxSmush = 0
    private var curCharWidth = 0
    private var prevCharWidth = 0
    private var currentTotalWidth = 0

    fun drawText(
        text: String,
        fontDirectory: File? = if (SystemUtils.IS_OS_WINDOWS) File("c:/Windows/Fonts") else null,
        fontName: String,
        fontSize: Int,
        backgroundColor: Color<*>,
        textColor: Color<*>,
    ): BufferedImage {
        val frameBufferTemp = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
        val gTemp: Graphics2D = frameBufferTemp.graphics as Graphics2D

        val f = loadFont(fontDirectory, fontName)
        val font = Font(f.name, 0, fontSize)
        val fontMetrics = gTemp.getFontMetrics(font)
        val width = fontMetrics.stringWidth(text)
        val height = fontMetrics.height

        val frameBuffer = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g: Graphics2D = frameBuffer.graphics as Graphics2D
        g.color = backgroundColor.toAwtColor()
        g.fillRect(0, 0, width, height)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.font = font
        g.color = textColor.toAwtColor()
        g.drawString(text, 0, height - fontMetrics.descent)

        return frameBuffer
    }

    /**
     * Loads the font with the given family name from the given directory.
     * The fontDirectory defaults to the current os system fonts directory
     * (windows and mac os x only for now).
     */
    private fun loadFont(
        fontDirectory: File? = systemFontsDirectory(),
        fontName: String
    ): Font {
        val ins = fontDirectory
            ?.let { File(it, fontName).inputStream() }
            ?:ClassLoader.getSystemClassLoader().getResourceAsStream(fontName)
        return ins.use { fontResource ->
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val font = Font.createFont(Font.TRUETYPE_FONT, fontResource)
            ge.registerFont(font)
            font
        }
    }

    /**
     * Returns the system font directory for the current platform (currently only windows or mac os x).
     */
    private fun systemFontsDirectory(): File? {
        return if (SystemUtils.IS_OS_WINDOWS) {
            File("c:/Windows/Fonts")
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            Paths.get(SystemUtils.USER_DIR, "Library", "Fonts").toFile()
        } else {
            null
        }
    }

    fun drawFigletText(
        text: String,
        textWidth: Int = 9999,
        fontName: String = "basic",
        direction: Direction = Direction.auto,
        backgroundColor: Color<*> = RGBColor(0, 0, 0),
        textColor: Color<*> = RGBColor(255, 0, 0),
        justify: Justify = Justify.auto,
        ensureWidth: Boolean = false
    ): XledFrame {
       iterator = 0
       maxSmush = 0
       curCharWidth = 0
       prevCharWidth = 0
       currentTotalWidth = 0

        val font = FigletFont("fonts/$fontName.flf")
        val buffer = Array(font.height) { "" }
        val chars: List<Int> = text.map { it.code }
        val blankMarkers = mutableListOf<Pair<Array<String>, Int>>()
        val queue = mutableListOf<Array<String>>()
        val smusher = FigletSmusher(direction, font)


        while (iterator < text.length) {
            addChar(buffer, chars, queue, blankMarkers, font, smusher, textWidth)
            iterator += 1
        }
        if (buffer[0].isNotEmpty()) {
            queue.add(buffer)
        }
        var charMatrix = queue.map { buff ->
            justifyString(justify, buff, textWidth)
                .map { it.replace(font.hardBlank, " ") }
        }
        val rows = charMatrix.size
        val rowHeight = charMatrix.maxOf { it.size }
        val height = rows * rowHeight
        charMatrix = trimLeft(charMatrix)
        val width = charMatrix.maxOf { line -> line.maxOf { row -> row.length } }
        val frame = XledFrame(width, height, backgroundColor)
        charMatrix.forEachIndexed { l, line ->
            line.forEachIndexed { y, row ->
                row.forEachIndexed { x, char ->
                    if (char != ' ') frame[x, y] = textColor
                }
            }
        }
        ensureWidth(ensureWidth, width, textWidth, justify, frame)

        return frame
    }

    private fun ensureWidth(
        ensureWidth: Boolean,
        width: Int,
        textWidth: Int,
        justify: Justify,
        frame: XledFrame
    ) {
        if (ensureWidth && width < textWidth) {
            when (justify) {
                Justify.auto, Justify.left -> {
                    frame.expandRight(textWidth - width)
                }

                Justify.right -> {
                    frame.expandLeft(textWidth - width)
                }

                Justify.center -> {
                    val diff = textWidth - width
                    val left = diff / 2
                    val right = diff - left
                    frame.expandLeft(left)
                    frame.expandRight(right)
                }
            }
        }
    }

    private fun trimLeft(
        charMatrix: List<List<String>>
    ): List<List<String>> {
        val spaces = charMatrix.flatMap { line -> line.map { row -> "^ +".toRegex().find(row)?.value?.length ?: 0 } }.min()
        return charMatrix.map { line -> line.map { it.drop(spaces) } }
    }

    private fun addChar(
        buffer: Array<String>,
        chars: List<Int>,
        queue: MutableList<Array<String>>,
        blankMarkers: MutableList<Pair<Array<String>, Int>>,
        font: FigletFont,
        smusher: FigletSmusher,
        textWidth: Int
    ) {
        val tuple = Pair(buffer.clone(), iterator)
        val code = chars[iterator]
        if (code == '\n'.code) {
            blankMarkers.add(tuple)
            handleNewline(buffer, chars, queue, blankMarkers, font, smusher)
        } else {
            val curChar = font.chars[code]
            if (curChar != null) {
                curCharWidth = font.width[code] ?:0
                if (textWidth < curCharWidth) {
                    throw IllegalStateException("No space left to print char")
                }
                maxSmush = if (curChar.isNotEmpty()) {
                    smusher.currentSmushAmount(buffer, curChar, curCharWidth, prevCharWidth)
                } else {
                    0
                }
                currentTotalWidth = buffer[0].length + curCharWidth - maxSmush
                if (code == ' '.code) {
                    blankMarkers.add(tuple)
                }
                if (currentTotalWidth >= textWidth) {
                    handleNewline(buffer, chars, queue, blankMarkers, font, smusher)
                } else {
                    for (row in 0 until font.height) {
                        val (addLeft, addRight) = smusher.smushRow(buffer[row], curChar, row, maxSmush, curCharWidth, prevCharWidth)
                        buffer[row] = addLeft + addRight.substring(maxSmush)
                    }
                }
                prevCharWidth = curCharWidth
            }
        }
    }

    private fun handleNewline(
        buffer: Array<String>,
        chars: List<Int>,
        queue: MutableList<Array<String>>,
        blankMarkers: MutableList<Pair<Array<String>, Int>>,
        font: FigletFont,
        smusher: FigletSmusher
    ) {
        if (blankMarkers.isNotEmpty()) {
            val (savedBuffer, savedIterator) = blankMarkers.first()
            blankMarkers.removeAt(0)
            queue.add(savedBuffer)
            iterator = savedIterator
        } else {
            queue.add(buffer)
            iterator -= 1
        }

        currentTotalWidth = buffer[0].length
        buffer.fill("", 0, font.height)
        blankMarkers.clear()
        prevCharWidth = 0
        val curChar = font.chars[chars[iterator]]
        if (curChar?.isNotEmpty() == true) {
            maxSmush = smusher.currentSmushAmount(buffer, curChar, curCharWidth, prevCharWidth)
        }
    }

    private fun justifyString(
        justify: Justify,
        buffer: Array<String>,
        textWidth: Int
    ): Array<String> {
        return when (justify) {
            Justify.right -> buffer.map { row -> " ".repeat((textWidth - row.length)) + row }.toTypedArray()
            Justify.center -> buffer.map { row -> " ".repeat((textWidth - row.length) / 2) + row }.toTypedArray()
            else -> buffer
        }
    }
}
