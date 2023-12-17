package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.font.Direction
import de.visualdigits.kotlin.twinkly.model.font.FigletFont
import de.visualdigits.kotlin.twinkly.model.font.FigletSmusher
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import org.apache.commons.lang3.SystemUtils
import java.awt.Font
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths


object FontUtil {

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

    fun drawFigletText(
        text: String,
        fontName: String = "basic",
        backgroundColor: Color<*> = RGBColor(0, 0, 0),
        textColor: Color<*> = RGBColor(255, 0, 0),
    ): XledFrame {
        val font = FigletFont("fonts/$fontName.flf")
        val smusher = FigletSmusher(Direction.auto, font)
        val chars = renderText(text, font, smusher)
        val frame = XledFrame(chars.firstOrNull()?.length?:0, chars.size, backgroundColor)
        chars.forEachIndexed { y, line ->
            line.forEachIndexed { x, char ->
                if (char != ' ') frame[x, y] = textColor
            }
        }

        return frame
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

    private fun renderText(
        text: String,
        font: FigletFont,
        smusher: FigletSmusher
    ): List<String> {
        var curCharWidth: Int
        var maxSmush: Int
        var prevCharWidth = 0
        val buffer = Array(font.height) { "" }

        text.forEach { c ->
            val code = c.code
            val curChar: List<String>? = font.chars[code]
            if (curChar != null) {
                curCharWidth = font.width[code] ?:0
                maxSmush = if (curChar.isNotEmpty()) {
                    smusher.currentSmushAmount(buffer, curChar, curCharWidth, prevCharWidth)
                } else {
                    0
                }
                for (row in 0 until font.height) {
                    val (addLeft, addRight) = smusher.smushRow(buffer[row], curChar, row, maxSmush, curCharWidth, prevCharWidth)
                    buffer[row] = addLeft + addRight.substring(maxSmush)
                }
                prevCharWidth = curCharWidth
            }
        }
        return buffer
            .map { it.replace(font.hardBlank, " ")}
//            .filter { it.trim().isNotEmpty() }
    }
}
