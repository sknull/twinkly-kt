@file:OptIn(ExperimentalStdlibApi::class)

package de.visualdigits.kotlin.twinkly.model.playable.transition

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.playable.Playable
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random

enum class TransitionType(
    private val transitionHandler: Transition
) {

    STRAIGHT(TransitionStraight()),
    FADE(TransitionFade()),
    WIPE(TransitionWipe()),
    CURTAIN_OPEN(TransitionCurtainOpen()),
    CURTAIN_CLOSE(TransitionCurtainClose()),
    DISC(TransitionDisc()),
    RANDOM(TransitionRandom())
    ;

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val random = Random(System.currentTimeMillis())

    companion object {
        fun random(): TransitionType {
            val e = entries.toMutableList()
            e.remove(STRAIGHT)
            return e.random()
        }
    }

    fun supportedTransitionDirections(): List<TransitionDirection> = transitionHandler.supportedTransitionDirections()

    fun nextFrame(
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        transitionDirection: TransitionDirection,
        blendMode: BlendMode,
        factor: Double
    ): XledFrame = transitionHandler.nextFrame(
        sourceFrame = sourceFrame,
        targetFrame = targetFrame,
        transitionDirection = transitionDirection,
        blendMode = blendMode,
        factor = factor
    )

    fun transitionSequence(
        source: Playable,
        target: Playable,
        transitionDirection: TransitionDirection = TransitionDirection.LEFT_RIGHT,
        blendMode: BlendMode = BlendMode.REPLACE,
        frameDelay: Long = 100,
        duration: Long = 2000
    ): XledSequence {

        val availableTransitions = TransitionType.entries.toMutableList()
        availableTransitions.remove(RANDOM)
        val transition = if (this == RANDOM) availableTransitions[random.nextInt(0, availableTransitions.size)] else this
        val direction = if (this == RANDOM) TransitionDirection.entries[random.nextInt(0, TransitionDirection.entries.size)] else transitionDirection
        val mode = if (this == RANDOM) BlendMode.entries[random.nextInt(0, BlendMode.entries.size)] else blendMode

        log.debug("#### ${transitionHandler.javaClass.simpleName}: $transitionDirection, $transitionDirection, $blendMode")

        return transition.transitionHandler.transitionSequence(
            source = source,
            target = target,
            transitionDirection = direction,
            blendMode = mode,
            frameDelay = frameDelay,
            duration = duration
        )
    }
}
