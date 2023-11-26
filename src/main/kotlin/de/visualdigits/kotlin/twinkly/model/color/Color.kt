package de.visualdigits.kotlin.twinkly.model.color

interface Color<T : Color<T>> {

    fun value(): Long

    fun hex(): String

    fun web(): String

    fun ansiColor(): String

    fun toRGB(): RGBColor

    fun toHSV(): HSVColor

    fun toRGBW(): RGBWColor

    fun toRGBA(): RGBAColor

    fun parameterMap(): Map<String, Int>

    fun clone(): T

    /**
     * Fades this instance towards the given instance using the given factor 0.0 .. 1.0.
     */
    fun fade(other: Any, factor: Double): T

    fun isBlack(): Boolean
}
