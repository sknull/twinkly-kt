package de.visualdigits.kotlin.util

import java.awt.Color
import java.awt.Font
import java.awt.FontFormatException
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.Paint
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.IOException


object FontUtil {

    fun drawText(
        frameBuffer: BufferedImage,
        text: String,
        size: Int,
        fontName: String,
        color: Color,
        x: Int,
        y: Int,
        center: Boolean
    ): Rectangle {
        var px = x
        var py = y
        val w: Int = frameBuffer.width
        val h: Int = frameBuffer.height
        val g: Graphics2D = frameBuffer.graphics as Graphics2D
        val oldHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val font = Font(fontName, 0, size)
        val fontMetrics = g.getFontMetrics(font)
        val height = fontMetrics.height
        val width = fontMetrics.stringWidth(text)
        g.font = font
        g.color = color
        if (px < 0) {
            px = ((w - width) / 2.0).toInt()
        }
        else if (center) {
            px = (px - width / 2.0).toInt()
        }
        if (py < 0) {
            py = (h - (h - height) / 2.0).toInt()
        }
        g.drawString(text, px, py)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldHint)
        return Rectangle(px, py, width, height)
    }

    fun loadFont(fontName: String): Font? {
        var font: Font? = null
        try {
            ClassLoader.getSystemClassLoader().getResourceAsStream(fontName).use { fontResource ->
                val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
                javaClass.getClassLoader()
                if (fontResource != null) {
                    font = Font.createFont(Font.TRUETYPE_FONT, fontResource)
                    ge.registerFont(font)
                }
                else {
                    throw IllegalStateException("Could not load font $fontName")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: FontFormatException) {
            e.printStackTrace()
        }
        return font
    }

}
