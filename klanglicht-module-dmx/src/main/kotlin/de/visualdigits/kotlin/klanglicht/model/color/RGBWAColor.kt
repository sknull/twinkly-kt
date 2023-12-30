package de.visualdigits.kotlin.klanglicht.model.color

import org.apache.commons.lang3.StringUtils
import java.lang.Long.decode
import kotlin.math.min
import kotlin.math.roundToInt

class RGBWAColor(
    red: Int = 0,
    green: Int = 0,
    blue: Int = 0,
    var white: Int = 0,
    var amber: Int = 0
) : RGBBaseColor<RGBWAColor>(red, green, blue) {

    constructor(value: Long) : this(
        red = min(a = 255, b = (value and 0xff00000000L shr 32).toInt()),
        green = min(a = 255, b = (value and 0x00ff000000L shr 24).toInt()),
        blue = min(a = 255, b = (value and 0x0000ff0000L shr 16).toInt()),
        white = min(a = 255, b = (value and 0x000000ff00L shr 8).toInt()),
        amber = min(a = 255, b = (value and 0x00000000ffL).toInt()),
    )

    constructor(hex: String) : this(decode(if (hex.startsWith("#") || hex.startsWith("0x")) hex else "#$hex"))

    override fun toString(): String {
        return "[" + StringUtils.join(listOf(red, green, blue, white, amber), ", ") + "]"
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

    override fun fade(other: Any, factor: Double): RGBWAColor {
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

    override fun hex(): String = StringUtils.leftPad(java.lang.Long.toHexString(value()), 10, '0')

    override fun web(): String = "#${hex()}"

    override fun ansiColor(): String {
        return toRGB().ansiColor()
    }

    override fun toRGB(): RGBColor {
        return RGBColor(
            red = min(255, red + (amber / RGBAColor.AMBER_FACTOR).roundToInt()) + white,
            green = min(255, green + (amber * RGBAColor.AMBER_FACTOR).roundToInt() + white),
            blue = min(255, blue + white)
        )
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
