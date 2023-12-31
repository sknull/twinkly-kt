package de.visualdigits.kotlin.klanglicht.model.parameter

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences

interface Fadeable<T : Fadeable<T>> {

    fun getId(): String = ""

    fun getTurnOn(): Boolean? = false

    fun setTurnOn(turnOn: Boolean) {
    }

    fun getGain(): Float = 1.0f

    fun setGain(gain: Float) {
    }

    fun getRgbColor(): RGBColor? = null

    fun setRgbColor(rgbColor: RGBColor) {
    }

    fun fade(
        other: T,
        fadeDuration: Long,
        preferences: Preferences
    ) {
        val dmxFrameTime = preferences.getDmxFrameTime()
        val step = 1.0 / fadeDuration.toDouble() * dmxFrameTime.toDouble()
        var factor = 0.0

        while (factor < 1.0) {
            val faded = fade(other, factor)
            faded.write(preferences)
            factor += step
            Thread.sleep(dmxFrameTime)
        }
        other.write(preferences)
    }

    fun write(preferences: Preferences, write: Boolean = true, transitionDuration: Long = 1) {
        // nothing to do here
    }

    /**
     * Fades this instance towards the given instance using the given factor 0.0 .. 1.0.
     */
    fun fade(other: Any, factor: Double): T
}
