package de.visualdigits.kotlin.twinkly.model.color

interface Color<T : Color<T>> {

    fun clone(): T

    fun value(): Long

    fun isBlack(): Boolean

    fun hex(): String

    fun web(): String

    fun ansiColor(): String

    fun toRgbColor(): RGBColor

    fun toHsvColor(): HSVColor

    fun toRgbwColor(): RGBWColor

//    fun toRgbaColor(): RGBAColor

    fun toAwtColor(): java.awt.Color

    fun parameterMap(): Map<String, Int>

    /**
     * Blends this color towards the given color according to its alpha value of the given color.
     */
    fun blend(other: Any, blendMode: BlendMode): T

    /**
     * Fades this instance towards the given instance using the given factor 0.0 .. 1.0.
     */
    fun fade(other: Any, factor: Double, blendMode: BlendMode): T
}
