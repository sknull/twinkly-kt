package de.visualdigits.kotlin.twinkly.model.parameter

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.RGBColor

interface Fadeable<T : Fadeable<T>> {

    fun clone(): T

    fun getId(): String = ""

    fun toRgbColor(): RGBColor

    fun setRgbColor(rgbColor: RGBColor) {
        // do something
    }

    fun getGain(): Double = 1.0

    fun setGain(gain: Double) {
        // do something
    }

    fun getTurnOn(): Boolean? = false

    fun setTurnOn(turnOn: Boolean?) {
        // do something
    }

    fun fade(
        other: T,
        fadeDuration: Long,
        frameTime: Long = 40L
    ) {
        if (fadeDuration > 0) {
            val step = 1.0 / fadeDuration.toDouble() * frameTime
            var factor = 0.0

            while (factor <= 1.0) {
                val faded = fade(other, factor, BlendMode.AVERAGE)
                faded.write()
                factor += step
                Thread.sleep(frameTime)
            }
        }
        other.write()
    }

    /**
     * Fades this instance towards the given instance using the given factor 0.0 .. 1.0.
     */
    fun fade(other: Any, factor: Double, blendMode: BlendMode): T

    fun write(write: Boolean = true, transitionDuration: Long = 1L) {
        // do something
    }
}
