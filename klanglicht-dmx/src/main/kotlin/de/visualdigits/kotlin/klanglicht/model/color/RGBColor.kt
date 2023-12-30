package de.visualdigits.kotlin.klanglicht.model.color

import kotlin.math.min
import kotlin.math.roundToInt


class RGBColor(
   red: Int = 0,
   green: Int = 0,
   blue: Int = 0
) : RGBBaseColor<RGBColor>(red, green, blue) {

    constructor(value: Long) : this(
        red = min(a = 255, b = (value and 0x00ff0000L shr 16).toInt()),
        green = min(a = 255, b = (value and 0x0000ff00L shr 8).toInt()),
        blue = min(a = 255, b = (value and 0x000000ffL).toInt())
    )

    constructor(hex: String) : this(java.lang.Long.decode(if (hex.startsWith("#") || hex.startsWith("0x")) hex else "#$hex"))

    override fun parameterMap(): Map<String, Int> = mapOf(
        "Red" to red,
        "Green" to green,
        "Blue" to blue,
    )

    override fun fade(other: Any, factor: Double): RGBColor {
        return if (other is RGBColor) {
            RGBColor(
                red = min(255, (red + factor * (other.red - red)).roundToInt()),
                green =  min(255, (green + factor * (other.green - green)).roundToInt()),
                blue =  min(255, (blue + factor * (other.blue - blue)).roundToInt())
            )
        } else throw IllegalArgumentException("Cannot not fade another type")
    }
}
