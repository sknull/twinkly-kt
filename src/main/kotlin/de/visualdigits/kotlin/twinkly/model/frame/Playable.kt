package de.visualdigits.kotlin.twinkly.model.frame

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.frame.transition.TransitionDirection
import de.visualdigits.kotlin.twinkly.model.frame.transition.TransitionType
import de.visualdigits.kotlin.twinkly.model.xled.XLed

interface Playable {

    fun play(
        xled: XLed,
        loop: Int = -1, // no loop
        random: Boolean = false,
        transitionType: TransitionType? = null,
        transitionDirection: TransitionDirection? = null,
        transitionBlendMode: BlendMode? = null,
        transitionDuration: Long = 2550,
        verbose: Boolean = false
    )

    fun stop()

    fun frames(): List<Playable>

    fun firstFrame(): XledFrame? {
        return when (this) {
            is XledFrame -> this
            is XledSequence -> {
                when (val lf = frames().firstOrNull()) {
                    is XledFrame -> lf
                    is XledSequence -> lf.firstFrame()
                    else -> null
                }
            }
            else -> null
        }
    }

    fun lastFrame(): XledFrame? {
        return when (this) {
            is XledFrame -> this
            is XledSequence -> {
                when (val lf = frames().lastOrNull()) {
                    is XledFrame -> lf
                    is XledSequence -> lf.lastFrame()
                    else -> null
                }
            }
            else -> null
        }
    }

    fun toByteArray(bytesPerLed: Int): ByteArray
}
