package de.visualdigits.kotlin.klanglicht.model.color

import kotlin.math.floor
import kotlin.math.roundToInt

class HSVColor(
    /** 0-360 */
    var h: Int = 0,
    /** 0-100 */
    var s: Int = 0,
    /** 0 -100 */
    var v: Int = 0
) : Color<HSVColor> {

    override fun toString(): String {
        return "[$h, $s, $v]"
    }

    fun repr(): String {
        return "RGBColor(hex='${web()}', h=$h, s=$s , v=$v)"
    }

    override fun parameterMap(): Map<String, Int> {
        val rgbColor = toRGB()
        return mapOf(
            "Red" to rgbColor.red,
            "Green" to rgbColor.green,
            "Blue" to rgbColor.blue,
        )
    }

    override fun fade(other: Any, factor: Double): HSVColor {
        return if (other is HSVColor) {
            other.toRGB().fade(other.toRGB(), factor).toHSV()
        } else throw IllegalArgumentException("Cannot not fade another type")
    }

    override fun toRGB(): RGBColor {
        val h = (this.h / 360.0)
        val s = (this.s / 100.0)
        val v = (this.v / 100.0)
        val components = (if (s == 0.0) {
            listOf(v, v, v)
        } else {
            val varH = h * 6
            val varI = floor(varH)
            val var1 = v * (1 - s)
            val var2 = v * (1 - s * (varH - varI))
            val var3 = v * (1 - s * (1 - (varH - varI)))

            when (varI) {
                0.0 -> listOf(v, var3, var1)
                1.0 -> listOf(var2, v, var1)
                2.0 -> listOf(var1, v, var3)
                3.0 -> listOf(var1, var2, v)
                4.0 -> listOf(var3, var1, v)
                else -> listOf(v, var1, var2)
            }
        }).map { 255.coerceAtMost((it * 255.0).roundToInt()) }

        return RGBColor(
            red = components[0],
            green = components[1],
            blue = components[2]
        )
    }

    override fun value(): Long {
        return toRGB().value()
    }

    override fun hex(): String {
        return toRGB().hex()
    }

    override fun web(): String {
        return toRGB().web()
    }

    override fun ansiColor(): String {
        return toRGB().ansiColor()
    }

    override fun toHSV(): HSVColor {
        return toRGB().toHSV()
    }

    override fun toRGBW(): RGBWColor {
        return toRGB().toRGBW()
    }

    override fun toRGBA(): RGBAColor {
        return toRGB().toRGBA()
    }
}
