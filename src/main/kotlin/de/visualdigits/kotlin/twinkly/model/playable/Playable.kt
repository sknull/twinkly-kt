package de.visualdigits.kotlin.twinkly.model.playable

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionDirection
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionType

interface Playable {

    fun play(
        xled: de.visualdigits.kotlin.twinkly.model.device.xled.XLed,
        loop: Int = -1, // endless loop
        random: Boolean = false,
        transitionType: TransitionType? = TransitionType.STRAIGHT,
        transitionDirection: TransitionDirection? = TransitionDirection.LEFT_RIGHT,
        transitionBlendMode: BlendMode? = BlendMode.REPLACE,
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
