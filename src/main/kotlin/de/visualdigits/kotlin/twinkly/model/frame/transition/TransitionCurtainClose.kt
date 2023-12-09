package de.visualdigits.kotlin.twinkly.model.frame.transition

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import kotlin.math.roundToInt

class TransitionCurtainClose : Transition() {

    override fun supportedTransitionDirections(): List<TransitionDirection> = listOf(
        TransitionDirection.HORIZONTAL,
        TransitionDirection.VERTICAL
    )

    override fun nextFrame(
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        transitionDirection: TransitionDirection,
        blendMode: BlendMode,
        factor: Double
    ): XledFrame {
        val newTargetFrame = sourceFrame.clone()
        val width = sourceFrame.width
        val height = sourceFrame.height
        when (transitionDirection) {
            TransitionDirection.HORIZONTAL -> {
                val n = (width / 2 * factor).roundToInt()
                for (y in 0 until height) {
                    for (x in 0 until n) {
                        newTargetFrame[x][y] =  sourceFrame[x][y].fade(targetFrame[x][y], factor, blendMode)
                        newTargetFrame[width - 1 - x][y] =  sourceFrame[width - 1 - x][y].fade(targetFrame[width - 1 - x][y], factor, blendMode)
                    }
                }
            }
            TransitionDirection.VERTICAL -> {
                val n = (height / 2 * factor).roundToInt()
                for (x in 0 until width) {
                    for (y in 0 until n) {
                        newTargetFrame[x][y] = sourceFrame[x][y].fade(targetFrame[x][y], factor, blendMode)
                        newTargetFrame[x][height - 1 - y] = sourceFrame[x][height - 1 - y].fade(targetFrame[x][height - 1 - y], factor, blendMode)
                    }
                }
            }
            else -> {}
        }
        return newTargetFrame
    }

    override fun frameDelay(frameDelay: Long): Long {
        return frameDelay * 2
    }
}
