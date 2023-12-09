package de.visualdigits.kotlin.twinkly.model.frame.transition

import de.visualdigits.kotlin.twinkly.model.frame.Playable
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.frame.XledSequence
import java.lang.IllegalArgumentException

abstract class Transition {

    open fun supportedTransitionDirections(): List<TransitionDirection> = TransitionDirection.entries

    fun transitionSequence(
        source: Playable,
        target: Playable,
        transitionDirection: TransitionDirection = TransitionDirection.LEFT_RIGHT,
        frameDelay: Long = 100,
        duration: Long = 2000
    ): XledSequence {
        val sequence = XledSequence(frameDelay = frameDelay)
        if (supportedTransitionDirections().contains(transitionDirection)) {
            val steps = duration / frameDelay
            val s = (255 / steps).toInt()
            val sourceFrame = source.lastFrame()
            val targetFrame = target.firstFrame()
            sourceFrame
                ?.let { sf -> targetFrame
                    ?.let { tf ->
                        for (i in 0 .. 255 step s) {
                            nextFrame(sf, tf, i / 255.0)
                                ?.let { nf -> sequence.add(nf) }
                        }
                    }
                }
        }
        return sequence
    }

    abstract fun nextFrame(sourceFrame: XledFrame, targetFrame: XledFrame, factor: Double): XledFrame?
}
