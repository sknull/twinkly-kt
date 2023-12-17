package de.visualdigits.kotlin.twinkly.model.font

import de.visualdigits.kotlin.util.FontUtil
import org.junit.jupiter.api.Test

class FigletFontTest {

    @Test
    fun testTextMatrix() {
        val frame = FontUtil.drawFigletText(
            text = "Hello World!",
            fontName = "xttyb"
        )
        println(frame)
    }
}
