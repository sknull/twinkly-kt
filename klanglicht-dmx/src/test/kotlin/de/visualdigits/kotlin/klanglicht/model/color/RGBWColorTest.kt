package de.visualdigits.kotlin.klanglicht.model.color

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RGBWColorTest {

    @Test
    fun convertTest() {
        val rgb = RGBColor(255, 128, 128)
        val rgbw = rgb.toRGBW()
        assertEquals("[127, 0, 0, 128]", rgbw.toString())
        val rgb2: RGBColor = rgbw.toRGB()
        assertEquals(rgb.toString(), rgb2.toString())
    }

    @Test
    fun rgbwTest() {
        val rgbwColor = RGBWColor(255, 0, 0)
        assertEquals("[255, 0, 0, 0]", rgbwColor.toString())
        assertEquals("[0, 255, 0, 0]", RGBWColor(0, 255, 0).toString())
        assertEquals("[0, 0, 255, 0]", RGBWColor(0, 0, 255).toString())
        assertEquals("[255, 255, 0, 0]", RGBWColor(255, 255, 0).toString())
        assertEquals("[255, 0, 255, 0]", RGBWColor(255, 0, 255).toString())
        assertEquals("[0, 255, 255, 0]", RGBWColor(0, 255, 255).toString())
        assertEquals("[0, 0, 0, 0]", RGBWColor(0, 0, 0).toString())
        assertEquals("[0, 0, 0, 255]", RGBWColor(255, 255, 255).toString())
        assertEquals("[255, 128, 0, 0]", RGBWColor(255, 128, 0).toString())
        assertEquals("[64, 191, 0, 64]", RGBWColor(128, 255, 64).toString())
        assertEquals("[150, 0, 205, 50]", RGBWColor(200, 50, 255).toString())
    }

    @Test
    fun rgbwHexTest() {
        assertEquals("[255, 0, 0, 0]", RGBWColor("#ff000000").toString())
        assertEquals("[0, 255, 0, 0]", RGBWColor("#00ff0000").toString())
        assertEquals("[0, 0, 255, 0]", RGBWColor("#0000ff00").toString())
        assertEquals("[255, 255, 0, 0]", RGBWColor("#ffff0000").toString())
        assertEquals("[255, 0, 255, 0]", RGBWColor("#ff00ff00").toString())
        assertEquals("[0, 255, 255, 0]", RGBWColor("#00ffff00").toString())
        assertEquals("[0, 0, 0, 0]", RGBWColor("#00000000").toString())
        assertEquals("[0, 0, 0, 255]", RGBWColor("#000000ff").toString())
        assertEquals("[255, 128, 0, 0]", RGBWColor("#ff800000").toString())
        assertEquals("[64, 191, 0, 64]", RGBWColor("#40bf0040").toString())
        assertEquals("[150, 0, 205, 50]", RGBWColor("#9600cd32").toString())
    }

    @Test
    fun mixTestTwoColors() {
        val color1 = RGBWColor("#ff000000")
        val color2 = RGBWColor("#0000ff00")
        val mixed = color1.fade(color2, 0.5)
        assertEquals("#80008000", mixed.web())
    }

    @Test
    fun mixTestDifferentWhites() {
        // tests that two colors containing white in different notations end up with white regardless of the facor
        val color1 = RGBWColor("#ffffff00")
        val color2 = RGBWColor("#000000ff")
        assertEquals("#000000ff", color1.fade(color2, 0.1).web())
        assertEquals("#000000ff", color1.fade(color2, 0.2).web())
        assertEquals("#000000ff", color1.fade(color2, 0.3).web())
        assertEquals("#000000ff", color1.fade(color2, 0.4).web())
        assertEquals("#000000ff", color1.fade(color2, 0.5).web())
        assertEquals("#000000ff", color1.fade(color2, 0.6).web())
        assertEquals("#000000ff", color1.fade(color2, 0.7).web())
        assertEquals("#000000ff", color1.fade(color2, 0.8).web())
        assertEquals("#000000ff", color1.fade(color2, 0.9).web())
    }
}
