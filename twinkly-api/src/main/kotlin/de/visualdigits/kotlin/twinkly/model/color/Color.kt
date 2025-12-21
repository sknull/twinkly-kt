package de.visualdigits.kotlin.twinkly.model.color

import de.visualdigits.kotlin.twinkly.model.parameter.Parameter

interface Color<T : Color<T>> : Parameter<T> {

    fun value(): Long

    fun isBlack(): Boolean

    fun web(): String

    fun ansiColor(): String

    fun toHsvColor(): HSVColor

    fun toAwtColor(): java.awt.Color

    /**
     * Blends this color towards the given color according to its alpha value of the given color.
     */
    fun blend(other: Any, blendMode: BlendMode): T

    fun multiply(factor: Double): T
}
