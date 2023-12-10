package de.visualdigits.kotlin.twinkly.model.frame.transition

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class TransitionWipe : Transition() {

    override fun supportedTransitionDirections(): List<TransitionDirection> = listOf(
        TransitionDirection.LEFT_RIGHT,
        TransitionDirection.RIGHT_LEFT,
        TransitionDirection.UP_DOWN,
        TransitionDirection.DOWN_UP,
        TransitionDirection.DIAGONAL_FROM_TOP_LEFT,
        TransitionDirection.DIAGONAL_FROM_TOP_RIGHT,
        TransitionDirection.DIAGONAL_FROM_BOTTOM_LEFT,
        TransitionDirection.DIAGONAL_FROM_BOTTOM_RIGHT
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
            TransitionDirection.LEFT_RIGHT -> {
                val n = (width * factor).roundToInt()
                for (y in 0 until height) {
                    for (x in 0 until n) {
                        newTargetFrame[x][y] =  sourceFrame[x][y].fade(targetFrame[x][y], factor, blendMode)
                    }
                }
            }
            TransitionDirection.RIGHT_LEFT -> {
                val n = (width * factor).roundToInt()
                for (y in 0 until height) {
                    for (x in 1 .. n) {
                        newTargetFrame[width - x][y] = sourceFrame[width - x][y].fade(targetFrame[width - x][y], factor, blendMode)
                    }
                }
            }
            TransitionDirection.UP_DOWN -> {
                val n = (height * factor).roundToInt()
                for (x in 0 until width) {
                    for (y in 0 until n) {
                        newTargetFrame[x][y] = sourceFrame[x][y].fade(targetFrame[x][y], factor, blendMode)
                    }
                }
            }
            TransitionDirection.DOWN_UP -> {
                val n = (height * factor).roundToInt()
                for (x in 0 until width) {
                    for (y in 1 .. n) {
                        newTargetFrame[x][height - y] = sourceFrame[x][height - y].fade(targetFrame[x][height - y], factor, blendMode)
                    }
                }
            }
            TransitionDirection.DIAGONAL_FROM_TOP_LEFT -> {
                val n = (min(width, height) * 2 * factor).roundToInt()
                for (y in 0 until n) {
                    for (x in 0 .. y) {
                        val xx = min(x, width - 1)
                        val yy = min(y - x, height - 1)
                        newTargetFrame[xx][yy] =  sourceFrame[xx][yy].fade(targetFrame[xx][yy], factor, blendMode)
                    }
                }
            }
            TransitionDirection.DIAGONAL_FROM_TOP_RIGHT -> {
                val n = (min(width, height) * 2 * factor).roundToInt()
                for (y in 0 until n) {
                    for (x in 0 .. y) {
                        val xx = max(0, min(width - x, width - 1))
                        val yy = min(y - x, height - 1)
                        newTargetFrame[xx][yy] =  sourceFrame[xx][yy].fade(targetFrame[xx][yy], factor, blendMode)
                    }
                }
            }
            TransitionDirection.DIAGONAL_FROM_BOTTOM_LEFT -> {
                val n = (min(width, height) * 2 * factor).roundToInt()
                for (y in 0 until n) {
                    for (x in 0 .. y) {
                        val xx = min(x, width - 1)
                        val yy = max(0, min(height - y + x, height - 1))
                        newTargetFrame[xx][yy] =  sourceFrame[xx][yy].fade(targetFrame[xx][yy], factor, blendMode)
                    }
                }
            }
            TransitionDirection.DIAGONAL_FROM_BOTTOM_RIGHT -> {
                val n = (min(width, height) * 2 * factor).roundToInt()
                for (y in 0 until n) {
                    for (x in 0 .. y) {
                        val xx = max(0, min(width - x, width - 1))
                        val yy = max(0, min(height - y + x, height - 1))
                        newTargetFrame[xx][yy] =  sourceFrame[xx][yy].fade(targetFrame[xx][yy], factor, blendMode)
                    }
                }
            }
            else -> {}
        }

        return newTargetFrame
    }
}
