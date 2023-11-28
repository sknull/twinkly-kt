package de.visualdigits.kotlin.twinkly.model.color

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class RGBColor(
   red: Int = 0,
   green: Int = 0,
   blue: Int = 0,
   alpha: Int = 255,
   normalize: Boolean = false /** Determines if the white is extracted from the other values or not. */
) : RGBBaseColor<RGBColor>(red, green, blue, alpha, normalize) {

    constructor(value: Long, normalize: Boolean = false) : this(
        red = min(a = 255, b = (value and 0x00ff0000L shr 16).toInt()),
        green = min(a = 255, b = (value and 0x0000ff00L shr 8).toInt()),
        blue = min(a = 255, b = (value and 0x000000ffL).toInt()),
        normalize = normalize
    )

    override fun clone(): RGBColor = RGBColor(red, green, blue, alpha, normalize)

    override fun parameterMap(): Map<String, Int> = mapOf(
        "Red" to red,
        "Green" to green,
        "Blue" to blue,
    )

    override fun blend(other: Any, blendMode: BlendMode): RGBColor {
        return if (other is RGBColor) {
            fade(other, other.alpha / 255.0, blendMode)
        } else throw IllegalArgumentException("Cannot not fade another type")
    }

    override fun fade(other: Any, factor: Double, blendMode: BlendMode): RGBColor {
        return if (other is RGBColor) {
            when (blendMode) {
                BlendMode.REPLACE -> {
                    other
                }
                BlendMode.AVERAGE -> {
                    RGBColor(
                        red = min(255, (red + factor * (other.red - red)).roundToInt()),
                        green = min(255, (green + factor * (other.green - green)).roundToInt()),
                        blue = min(255, (blue + factor * (other.blue - blue)).roundToInt())
                    )
                }
                BlendMode.ADD -> {
                    RGBColor(
                        red = min(255, (red + factor * (other.red)).roundToInt()),
                        green = min(255, (green + factor * (other.green)).roundToInt()),
                        blue = min(255, (blue + factor * (other.blue)).roundToInt())
                    )
                }
                BlendMode.SUBTRACT -> {
                    RGBColor(
                        red = max(0, (red - factor * (other.red)).roundToInt()),
                        green = max(0, (green - factor * (other.green)).roundToInt()),
                        blue = max(0, (blue - factor * (other.blue)).roundToInt())
                    )
                }
            }
        } else throw IllegalArgumentException("Cannot not fade another type")
    }
}
