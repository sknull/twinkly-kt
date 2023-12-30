package de.visualdigits.kotlin.twinkly.model.color

import org.apache.commons.lang3.StringUtils
import java.lang.Long.decode
import java.lang.Long.toHexString
import kotlin.math.max
import kotlin.math.min


abstract class RGBBaseColor<T : RGBBaseColor<T>>(
    var red: Int = 0,
    var green: Int = 0,
    var blue: Int = 0,
    var alpha: Int = 255,
    val normalize: Boolean = false /** Determines if the white is extracted from the other values or not. */
) : Color<T> {

    constructor(value: Long, normalize: Boolean = false) : this(
        red = min(a = 255, b = (value and 0x00ff0000L shr 16).toInt()),
        green = min(a = 255, b = (value and 0x0000ff00L shr 8).toInt()),
        blue = min(a = 255, b = (value and 0x000000ffL).toInt()),
        normalize = normalize
    )

    constructor(hex: String, normalize: Boolean = false) : this(
        value = decode(if (hex.startsWith("#") || hex.startsWith("0x")) hex else "#$hex"),
        normalize = normalize
    )

    override fun toString(): String {
        return "[$red, $green, $blue]"
    }

    override fun toAwtColor(): java.awt.Color {
        val rgbColor = toRGB()
        return java.awt.Color(rgbColor.red, rgbColor.green, rgbColor.blue)
    }

    open fun repr(): String {
        return "RGBColor(hex='${web()}', r=$red, g=$green , b=$blue)"
    }

    override fun isBlack(): Boolean = red == 0 && green == 0 && blue == 0

    override fun value(): Long = red.toLong() shl 16 or (green.toLong() shl 8) or blue.toLong()

    override fun hex(): String = StringUtils.leftPad(toHexString(value()), 6, '0')

    override fun web(): String = "#${hex()}"

    override fun ansiColor(): String = "\u001B[39m\u001B[48;2;$red;$green;${blue}m \u001B[0m"

    inline fun <reified T : Color<T>> convert(): T {
        return when (T::class) {
            RGBWColor::class -> toRGBW() as T
            HSVColor::class -> toHSV() as T
            RGBBaseColor::class -> toRGB() as T
            else -> throw IllegalStateException("Unsupported color type")
        }
    }

    override fun toRGB(): RGBColor {
        return RGBColor(
            red = red,
            green = green,
            blue = blue,
            alpha = alpha
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
        return HSVColor(
            h = h.toInt(),
            s = (100 * s).toInt(),
            v = (100 * max).toInt(),
            alpha = alpha
        )
    }

    override fun toRGBW(): RGBWColor {
        return if (normalize) {
            val white = min(red, min(green, blue))
            RGBWColor(
                red = red - white,
                green = green - white,
                blue = blue - white,
                white = white,
                alpha = alpha,
                normalize = normalize
            )
        } else {
            val hsv = toHSV()
            if (hsv.s == 0) {
                RGBWColor(
                    red = red,
                    green = green,
                    blue = blue,
                    white = min(red, min(green, blue)) / 2,
                    alpha = alpha,
                    normalize = normalize
                )
            } else {
                RGBWColor(
                    red = red,
                    green = green,
                    blue = blue,
                    white = 0,
                    alpha = alpha,
                    normalize = normalize
                )
            }
        }
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
