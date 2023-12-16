package de.visualdigits.kotlin.twinkly.model.playable.transition

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class TransitionDisc : Transition() {

    override fun supportedTransitionDirections(): List<TransitionDirection> = listOf(
        TransitionDirection.IN_OUT,
        TransitionDirection.OUT_IN,
    )

    override fun nextFrame(
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        transitionDirection: TransitionDirection,
        blendMode: BlendMode,
        factor: Double
    ): XledFrame {
        var newTargetFrame = sourceFrame.clone()
        val width = sourceFrame.width
        val height = sourceFrame.height
        val mx = width / 2
        val my = height / 2

        when (transitionDirection) {
            TransitionDirection.IN_OUT -> {
                val n = (max(width, height) * factor).roundToInt()
                for (r in 0 until n / 2) {
                    for (a in 0 until 360) {
                        val x = max(0, min(width - 1, (mx + r * cos(a * PI / 180.0)).roundToInt()))
                        val y = max(0, min(width - 1, (my + r * sin(a * PI / 180.0)).roundToInt()))
                        newTargetFrame[x, y] = sourceFrame[x, y].fade(targetFrame[x, y], factor, blendMode)
                    }
                }
            }

            TransitionDirection.OUT_IN -> {
                newTargetFrame = targetFrame.clone()
                val n = (max(width, height) * (1.0 - factor)).roundToInt()
                for (r in 0 until n / 2) {
                    for (a in 0 until 360) {
                        val x = max(0, min(width - 1, (mx + r * cos(a * PI / 180.0)).roundToInt()))
                        val y = max(0, min(width - 1, (my + r * sin(a * PI / 180.0)).roundToInt()))
                        newTargetFrame[x, y] = targetFrame[x, y].fade(sourceFrame[x, y], factor, blendMode)
                    }
                }
            }

            else -> {}
        }

        return newTargetFrame
    }
}
