package de.visualdigits.kotlin.twinkly.model.color

import java.lang.Long.decode
import java.lang.Long.toHexString
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


abstract class RGBBaseColor<T : RGBBaseColor<T>>(
    var red: Int = 0,
    var green: Int = 0,
    var blue: Int = 0,
    var alpha: Int = 255,
    val normalizeMode: NormalizeMode = NormalizeMode.NONE /** Determines if the white is extracted from the other values or not. */
) : Color<T> {

    constructor(value: Long, normalize: NormalizeMode = NormalizeMode.NONE) : this(
        red = min(a = 255, b = (value and 0x00ff0000L shr 16).toInt()),
        green = min(a = 255, b = (value and 0x0000ff00L shr 8).toInt()),
        blue = min(a = 255, b = (value and 0x000000ffL).toInt()),
        normalizeMode = normalize
    )

    constructor(hex: String, normalize: NormalizeMode = NormalizeMode.NONE) : this(
        value = decode(if (hex.startsWith("#") || hex.startsWith("0x")) hex else "#$hex"),
        normalize = normalize
    )

    override fun toString(): String {
        return "[$red, $green, $blue]"
    }

    open fun repr(): String {
        return "RGBColor(hex='${web()}', r=$red, g=$green , b=$blue)"
    }

    @Suppress("UNCHECKED_CAST") // I know...
    override fun clone(): T {
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
            else -> error("Unknown color type") // esoteric - make compiler happy
        }
    }

    override fun toAwtColor(): java.awt.Color {
        val rgbColor = toRgbColor()
        return java.awt.Color(rgbColor.red, rgbColor.green, rgbColor.blue)
    }

    override fun isBlack(): Boolean = red == 0 && green == 0 && blue == 0

    override fun value(): Long = red.toLong() shl 16 or (green.toLong() shl 8) or blue.toLong()

    override fun hex(): String = toHexString(value()).padStart(6, '0')

    override fun web(): String = "#${hex()}"

    override fun ansiColor(): String = "\u001B[39m\u001B[48;2;$red;$green;${blue}m \u001B[0m"

    inline fun <reified T : Color<T>> convert(): T {
        return when (T::class) {
            RGBWColor::class -> toRgbwColor() as T
            HSVColor::class -> toHsvColor() as T
            RGBBaseColor::class -> toRgbColor() as T
            else -> error("Unsupported color type")
        }
    }

    override fun toRgbColor(): RGBColor {
        return RGBColor(
            red = red,
            green = green,
            blue = blue,
            alpha = alpha
        )
    }

    override fun toHsvColor(): HSVColor {
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

    override fun toRgbwColor(): RGBWColor {
        return when (normalizeMode) {
            NormalizeMode.STANDARD -> {
                val white = min(red, min(green, blue))
                RGBWColor(
                    red = red - white,
                    green = green - white,
                    blue = blue - white,
                    white = white,
                    alpha = alpha
                )
            }
            NormalizeMode.FULL_ONLY -> {
                if (red == 255 && green == 255 && blue == 255) {
                    RGBWColor(
                        red = 0,
                        green = 0,
                        blue = 0,
                        white = 255,
                        alpha = alpha
                    )
                } else {
                    RGBWColor(
                        red = red,
                        green = green,
                        blue = blue,
                        white = 255,
                        alpha = alpha
                    )
                }
            }
            else -> {
                val hsv = toHsvColor()
                if (hsv.s == 0) {
                    RGBWColor(
                        red = red,
                        green = green,
                        blue = blue,
                        white = min(red, min(green, blue)) / 2,
                        alpha = alpha,
                        normalizeMode = normalizeMode
                    )
                } else {
                    RGBWColor(
                        red = red,
                        green = green,
                        blue = blue,
                        white = 0,
                        alpha = alpha,
                        normalizeMode = normalizeMode
                    )
                }
            }
        }
    }

    override fun toRgbaColor(): RGBAColor {
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
