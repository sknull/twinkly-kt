package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.model.color.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.image.BufferedImage


object FontUtil {

    fun drawText(
        text: String,
        size: Int,
        fontName: String,
        color: Color<*>,
    ): BufferedImage {
        val frameBufferTemp = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
        val gTemp: Graphics2D = frameBufferTemp.graphics as Graphics2D

        val f = loadFont(fontName)
        val font = Font(f.name, 0, size)
        val fontMetrics = gTemp.getFontMetrics(font)
        val width = fontMetrics.stringWidth(text)
        val height = fontMetrics.height


        val frameBuffer = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g: Graphics2D = frameBuffer.graphics as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.font = font
        g.color = color.toAwtColor()
        g.drawString(text, 0, height - fontMetrics.descent)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, g.getRenderingHint(RenderingHints.KEY_ANTIALIASING))

        return frameBuffer
    }

    private fun loadFont(fontName: String): Font {
        return ClassLoader.getSystemClassLoader().getResourceAsStream(fontName).use { fontResource ->
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val font = Font.createFont(Font.TRUETYPE_FONT, fontResource)
            ge.registerFont(font)
            font
        }
    }
}
