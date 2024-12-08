package de.visualdigits.kotlin.twinkly.model.color

import java.lang.Long.decode
import java.lang.Long.toHexString
import kotlin.math.min
import kotlin.math.roundToInt

class RGBWAColor(
    red: Int = 0,
    green: Int = 0,
    blue: Int = 0,
    var white: Int = 0,
    var amber: Int = 0,
    alpha: Int = 255,
    normalize: Boolean = false /** Determines if the white is extracted from the other values or not. */

) : RGBBaseColor<RGBWAColor>(red, green, blue, alpha) {

    constructor(rgb: Long, normalize: Boolean = false) : this(
        red = min(a = 255, b = (rgb and 0xff00000000L shr 32).toInt()),
        green = min(a = 255, b = (rgb and 0x00ff000000L shr 24).toInt()),
        blue = min(a = 255, b = (rgb and 0x0000ff0000L shr 16).toInt()),
        white = min(a = 255, b = (rgb and 0x000000ff00L shr 8).toInt()),
        amber = min(a = 255, b = (rgb and 0x00000000ffL).toInt()),
        normalize = normalize
    )

    constructor(hex: String, normalize: Boolean = false) : this(
        rgb = decode(if (hex.startsWith("#") || hex.startsWith("0x")) hex else "#$hex"),
        normalize = normalize
    )

    init {
        if (normalize && white == 0) {
            white = min(red, min(green, blue))
            super.red -= white
            super.green -= white
            super.blue -= white
        }
        if (normalize && amber == 0) {
            amber = min(red, green)
            super.red -= amber
            super.green -= amber
        }
    }

    override fun toString(): String {
        return "[" +listOf(red, green, blue, white, amber).joinToString(", ") + "]"
    }

    override fun repr(): String {
        return "RGBColor(hex='${web()}', r=$red, g=$green , b=$blue, w=$white, a=$amber)"
    }

    override fun parameterMap(): Map<String, Int> = mapOf(
        "Red" to red,
        "Green" to green,
        "Blue" to blue,
        "White" to white,
        "Amber" to amber
    )

    override fun blend(other: Any, blendMode: BlendMode): RGBWAColor {
        return if (other is RGBWAColor) {
            fade(other, other.alpha / 255.0, blendMode)
        } else throw IllegalArgumentException("Cannot not fade another type")
    }

    override fun fade(other: Any, factor: Double, blendMode: BlendMode): RGBWAColor {
        return if (other is RGBWAColor) {
            RGBWAColor(
                red = min(255, (red + factor * (other.red - red)).roundToInt()),
                green =  min(255, (green + factor * (other.green - green)).roundToInt()),
                blue =  min(255, (blue + factor * (other.blue - blue)).roundToInt()),
                white =  min(255, (white + factor * (other.white - white)).roundToInt()),
                amber =  min(255, (amber + factor * (other.amber - amber)).roundToInt())
            )
        } else throw IllegalArgumentException("Cannot not fade another type")
    }

    override fun value(): Long = (red.toLong() shl 32) or (green.toLong() shl 24) or (blue.toLong() shl 16) or (white.toLong() shl 8) or amber.toLong()

    override fun hex(): String = toHexString(value()).padStart( 10, '0')

    override fun web(): String = "#${hex()}"

    override fun ansiColor(): String {
        return toRgbColor().ansiColor()
    }

    override fun toRgbColor(): RGBColor {
        return RGBColor(
            red = min(255, red + (amber / RGBAColor.AMBER_FACTOR).roundToInt()) + white,
            green = min(255, green + (amber * RGBAColor.AMBER_FACTOR).roundToInt() + white),
            blue = min(255, blue + white)
        )
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
