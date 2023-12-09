package de.visualdigits.kotlin.twinkly.model.frame.transition

import de.visualdigits.kotlin.twinkly.model.frame.Playable
import de.visualdigits.kotlin.twinkly.model.frame.XledSequence

enum class TransitionType(
    private val transitionHandler: Transition
) {

    STRAIGHT(TransitionStraight()),
    FADE(TransitionFade())
    ;

    fun transitionSequence(
        source: Playable,
        target: Playable,
        transitionDirection: TransitionDirection= TransitionDirection.LEFT_RIGHT,
        frameDelay: Long = 100,
        duration: Long = 2000
    ): XledSequence {
        return transitionHandler.transitionSequence(
            source = source,
            target = target,
            transitionDirection = transitionDirection,
            frameDelay = frameDelay,
            duration = duration
        )
    }
}
