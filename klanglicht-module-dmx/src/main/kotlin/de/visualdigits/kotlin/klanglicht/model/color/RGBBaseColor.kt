package de.visualdigits.kotlin.klanglicht.model.color

import org.apache.commons.lang3.StringUtils
import java.lang.Long.decode
import java.lang.Long.toHexString
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


abstract class RGBBaseColor<T : RGBBaseColor<T>>(
    var red: Int = 0,
    var green: Int = 0,
    var blue: Int = 0
) : Color<T> {

    constructor(value: Long) : this(
        red = min(a = 255, b = (value and 0x00ff0000L shr 16).toInt()),
        green = min(a = 255, b = (value and 0x0000ff00L shr 8).toInt()),
        blue = min(a = 255, b = (value and 0x000000ffL).toInt())
    )

    constructor(hex: String) : this(decode(if (hex.startsWith("#") || hex.startsWith("0x")) hex else "#$hex"))

    override fun toString(): String {
        return "[" + StringUtils.join(listOf(red, green, blue), ", ") + "]"
    }

    open fun repr(): String {
        return "RGBColor(hex='${web()}', r=$red, g=$green , b=$blue)"
    }

    fun clone(): T {
        return when (this) {
            is RGBColor -> {
                RGBColor(red, green, blue) as T
            }
            is RGBWColor -> {
                RGBWColor(red, green, blue, white) as T
            }
            is RGBAColor -> {
                RGBAColor(red, green, blue, amber) as T
            }
            is RGBWAColor -> {
                RGBWAColor(red, green, blue, white, amber) as T
            }
            else -> throw IllegalStateException("Unknown color type") // esoteric - make compiler happy
        }
    }

    override fun value(): Long = red.toLong() shl 16 or (green.toLong() shl 8) or blue.toLong()

    override fun hex(): String = StringUtils.leftPad(toHexString(value()), 6, '0')

    override fun web(): String = "#${hex()}"

    override fun ansiColor(): String = "\u001B[39m\u001B[48;2;$red;$green;${blue}m \u001B[0m"

    override fun getRgbColor(): RGBColor? = clone().toRGB()

    override fun setRgbColor(rgbColor: RGBColor) {
        red = rgbColor.red
        green = rgbColor.green
        blue = rgbColor.blue
    }

    inline fun <reified T : Color<T>> convert(): T {
        return when (T::class) {
            RGBWColor::class -> toRGBW() as T
            RGBAColor::class -> toRGBA() as T
            HSVColor::class -> toHSV() as T
            RGBBaseColor::class -> toRGB() as T
            else -> throw IllegalStateException("Unsupported color type")
        }
    }

    override fun toRGB(): RGBColor {
        return RGBColor(
            red = red,
            green = green,
            blue = blue
        )
    }

    override fun toHSV(): HSVColor {
        val r = red.toDouble() / 255
        val g = green.toDouble() / 255
        val b = blue.toDouble() / 255
        val min = min(r, min(g, b))
        val max = max(r, max(g, b))
        val delta = max - min
        val s: Double
        var h: Double
        if (max == 0.0) {
            s = 0.0
            h = 0.0
        } else {
            s = delta / max
            h = if (r == max) {
                (g - b) / delta
            } else if (g == max) {
                2 + (b - r) / delta
            } else {
                4 + (r - g) / delta
            }
            h *= 60.0
            if (h < 0) {
                h += 360.0
            }
            if (java.lang.Double.isNaN(h)) {
                h = 0.0
            }
        }
        return HSVColor(h.toInt(), (100 * s).toInt(), (100 * max).toInt())
    }

    override fun toRGBW(): RGBWColor {
        val white = min(red, min(green, blue))
        return RGBWColor(
            red = red - white,
            green = green - white,
            blue = blue - white,
            white = white
        )
    }

    override fun toRGBA(): RGBAColor {
        val amber: Int
        var r = 0
        var g = 0
        if (red > green) {
            amber = green
            g = 0
            r -= (amber / RGBAColor.AMBER_FACTOR).roundToInt()
        } else {
            amber = red
            r = 0
            g -= (amber * RGBAColor.AMBER_FACTOR).roundToInt()
        }
        return RGBAColor(
            red = r - amber,
            green = g - amber,
            blue = blue,
            amber = amber
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RGBBaseColor<*>

        if (red != other.red) return false
        if (green != other.green) return false
        return blue == other.blue
    }

    override fun hashCode(): Int {
        var result = red
        result = 31 * result + green
        result = 31 * result + blue
        return result
    }
}
