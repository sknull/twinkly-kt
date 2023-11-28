package de.visualdigits.kotlin.twinkly.model.color

interface Color<T : Color<T>> {

    fun value(): Long

    fun hex(): String

    fun web(): String

    fun ansiColor(): String

    fun toRGB(): RGBColor

    fun toHSV(): HSVColor

    fun toRGBW(): RGBWColor

    fun parameterMap(): Map<String, Int>

    fun clone(): T

    /**
     * Blends this color towards the given color according to its alpha value of the given color.
     */
    fun blend(other: Any, blendMode: BlendMode): T

    /**
     * Fades this instance towards the given instance using the given factor 0.0 .. 1.0.
     */
    fun fade(other: Any, factor: Double, blendMode: BlendMode): T

    fun isBlack(): Boolean

    fun toAwtColor(): java.awt.Color
}
