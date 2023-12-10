package de.visualdigits.kotlin.twinkly.model.frame.transition

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.frame.Playable
import de.visualdigits.kotlin.twinkly.model.frame.XledSequence

enum class TransitionType(
    private val transitionHandler: Transition
) {

    STRAIGHT(TransitionStraight()),
    FADE(TransitionFade()),
    WIPE(TransitionWipe()),
    CURTAIN_OPEN(TransitionCurtainOpen()),
    CURTAIN_CLOSE(TransitionCurtainClose()),
    DISC(TransitionDisc())
    ;

    companion object {
        fun random(): TransitionType {
            val e = entries.toMutableList()
            e.remove(STRAIGHT)
            return e.random()
        }
    }

    fun supportedTransitionDirections(): List<TransitionDirection> = transitionHandler.supportedTransitionDirections()

    fun transitionSequence(
        source: Playable,
        target: Playable,
        transitionDirection: TransitionDirection = TransitionDirection.LEFT_RIGHT,
        blendMode: BlendMode = BlendMode.REPLACE,
        frameDelay: Long = 100,
        duration: Long = 2000
    ): XledSequence {
//println("#### ${transitionHandler.javaClass.simpleName}: $transitionDirection, $transitionDirection, $blendMode")
        return transitionHandler.transitionSequence(
            source = source,
            target = target,
            transitionDirection = transitionDirection,
            blendMode = blendMode,
            frameDelay = frameDelay,
            duration = duration
        )
    }
}
