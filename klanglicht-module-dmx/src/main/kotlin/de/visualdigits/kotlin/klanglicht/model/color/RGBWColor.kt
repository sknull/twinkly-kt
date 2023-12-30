package de.visualdigits.kotlin.klanglicht.model.color

import org.apache.commons.lang3.StringUtils
import java.lang.Long.decode
import kotlin.math.min
import kotlin.math.roundToInt

class RGBWColor(
    red: Int = 0,
    green: Int = 0,
    blue: Int = 0,
    var white: Int = 0
) : RGBBaseColor<RGBWColor>(red, green, blue) {

    constructor(value: Long) : this(
        red = min(a = 255, b = (value and 0xff000000L shr 24).toInt()),
        green = min(a = 255, b = (value and 0x00ff0000L shr 16).toInt()),
        blue = min(a = 255, b = (value and 0x0000ff00L shr 8).toInt()),
        white = min(a = 255, b = (value and 0x000000ffL).toInt()),
    )

    constructor(hex: String) : this(decode(if (hex.startsWith("#") || hex.startsWith("0x")) hex else "#$hex"))

    init {
        if (white == 0) {
            white = min(red, min(green, blue))
            super.red -= white
            super.green -= white
            super.blue -= white
        }
    }

    override fun toString(): String {
        return "[" + StringUtils.join(listOf(red, green, blue, white), ", ") + "]"
    }

    override fun repr(): String {
        return "RGBColor(hex='${web()}', r=$red, g=$green , b=$blue, w=$white)"
    }

    override fun parameterMap(): Map<String, Int> = mapOf(
        "Red" to red,
        "Green" to green,
        "Blue" to blue,
        "White" to white
    )

    override fun fade(other: Any, factor: Double): RGBWColor {
        return if (other is RGBWColor) {
            RGBWColor(
                red = min(255, (red + factor * (other.red - red)).roundToInt()),
                green =  min(255, (green + factor * (other.green - green)).roundToInt()),
                blue =  min(255, (blue + factor * (other.blue - blue)).roundToInt()),
                white =  min(255, (white + factor * (other.white - white)).roundToInt())
            )
        } else throw IllegalArgumentException("Cannot not fade another type")
    }

    override fun value(): Long = (red.toLong() shl 24) or (green.toLong() shl 16) or (blue.toLong() shl 8) or white.toLong()

    override fun hex(): String = StringUtils.leftPad(java.lang.Long.toHexString(value()), 8, '0')

    override fun web(): String = "#${hex()}"

    override fun ansiColor(): String {
        return toRGB().ansiColor()
    }

    override fun toRGB(): RGBColor {
        return RGBColor(
            red = min(255, red + white),
            green = min(255, green + white),
            blue = min(255, blue + white),
        )
    }

    override fun toHSV(): HSVColor {
        return toRGB().toHSV()
    }

    override fun toRGBW(): RGBWColor {
        return this
    }

    override fun toRGBA(): RGBAColor {
        return toRGB().toRGBA()
    }
}
