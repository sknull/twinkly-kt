package de.visualdigits.kotlin.twinkly.model.playable.transition

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.playable.Playable
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence

abstract class Transition {

    open fun supportedTransitionDirections(): List<TransitionDirection> = TransitionDirection.entries

    fun transitionSequence(
        source: Playable,
        target: Playable,
        transitionDirection: TransitionDirection = TransitionDirection.LEFT_RIGHT,
        blendMode: BlendMode = BlendMode.REPLACE,
        frameDelay: Long = 100,
        duration: Long = 2000
    ): XledSequence {
//println("#### ${javaClass.simpleName}: $transitionDirection, $transitionDirection, $blendMode")
        val fd = frameDelay(frameDelay)
        val sequence = XledSequence(frameDelay = fd)
        if (supportedTransitionDirections().contains(transitionDirection)) {
            val steps = duration / fd
            val s = (255 / steps).toInt()
            val sourceFrame = source.lastFrame()
            val targetFrame = target.firstFrame()
            sourceFrame
                ?.let { sf -> targetFrame
                    ?.let { tf ->
                        for (i in 0 .. 255 step s) {
                            val nextFrame = nextFrame(
                                sourceFrame = sf,
                                targetFrame = tf,
                                transitionDirection = transitionDirection,
                                blendMode = blendMode,
                                factor = i / 255.0
                            )
                            if (nextFrame != null) {
//                                println(nextFrame)
                                sequence.add(nextFrame)
                            }
                        }
                    }
                }
        }
        return sequence
    }

    open fun frameDelay(frameDelay: Long): Long {
        return frameDelay
    }

    abstract fun nextFrame(
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        transitionDirection: TransitionDirection,
        blendMode: BlendMode,
        factor: Double
    ): XledFrame?
}
