package de.visualdigits.kotlin.twinkly.model.color

import kotlin.math.floor
import kotlin.math.roundToInt

class HSVColor(
    /** 0-360 */
    var h: Int = 0,
    /** 0-100 */
    var s: Int = 0,
    /** 0 -100 */
    var v: Int = 0,
    var alpha: Int = 255
) : Color<HSVColor> {

    override fun toString(): String {
        return "[$h, $s, $v]"
    }

    fun repr(): String {
        return "RGBColor(hex='${web()}', h=$h, s=$s , v=$v)"
    }

    override fun toAwtColor(): java.awt.Color {
        val rgbColor = toRgbColor()
        return java.awt.Color(rgbColor.red, rgbColor.green, rgbColor.blue)
    }

    override fun clone(): HSVColor = HSVColor(h, s, v, alpha)

    override fun parameterMap(): Map<String, Int> {
        val rgbColor = toRgbColor()
        return mapOf(
            "Red" to rgbColor.red,
            "Green" to rgbColor.green,
            "Blue" to rgbColor.blue,
        )
    }

    override fun isBlack(): Boolean = toRgbColor().isBlack()

    override fun blend(other: Any, blendMode: BlendMode): HSVColor {
        return if (other is HSVColor) {
            fade(other, other.alpha / 255.0, blendMode)
        } else throw IllegalArgumentException("Cannot not fade another type")
    }

    override fun fade(other: Any, factor: Double, blendMode: BlendMode): HSVColor {
        return if (other is HSVColor) {
            other.toRgbColor().fade(other.toRgbColor(), factor, blendMode).toHsvColor()
        } else throw IllegalArgumentException("Cannot not fade another type")
    }

    override fun toRgbColor(): RGBColor {
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
            blue = components[2],
            alpha = alpha
        )
    }

    override fun value(): Long {
        return toRgbColor().value()
    }

    override fun hex(): String {
        return toRgbColor().hex()
    }

    override fun web(): String {
        return toRgbColor().web()
    }

    override fun ansiColor(): String {
        return toRgbColor().ansiColor()
    }

    override fun toHsvColor(): HSVColor {
        return toRgbColor().toHsvColor()
    }

    override fun toRgbwColor(): RGBWColor {
        return toRgbColor().toRgbwColor()
    }

    override fun toRgbaColor(): RGBAColor {
        return toRgbColor().toRgbaColor()
    }
}
