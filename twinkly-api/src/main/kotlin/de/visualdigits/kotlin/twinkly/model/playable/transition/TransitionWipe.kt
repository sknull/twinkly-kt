package de.visualdigits.kotlin.twinkly.model.playable.transition

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
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
                transitionLeftRight(width, factor, height, newTargetFrame, sourceFrame, targetFrame, blendMode)
            }
            TransitionDirection.RIGHT_LEFT -> {
                transitionRightLeft(width, factor, height, newTargetFrame, sourceFrame, targetFrame, blendMode)
            }
            TransitionDirection.UP_DOWN -> {
                transitionUpDown(height, factor, width, newTargetFrame, sourceFrame, targetFrame, blendMode)
            }
            TransitionDirection.DOWN_UP -> {
                transitionDownUp(height, factor, width, newTargetFrame, sourceFrame, targetFrame, blendMode)
            }
            TransitionDirection.DIAGONAL_FROM_TOP_LEFT -> {
                transitionDiagonalTopLeft(width, height, factor, newTargetFrame, sourceFrame, targetFrame, blendMode)
            }
            TransitionDirection.DIAGONAL_FROM_TOP_RIGHT -> {
                transitionDiagonalTopRight(width, height, factor, newTargetFrame, sourceFrame, targetFrame, blendMode)
            }
            TransitionDirection.DIAGONAL_FROM_BOTTOM_LEFT -> {
                transitionDiagonalBottomLeft(width, height, factor, newTargetFrame, sourceFrame, targetFrame, blendMode)
            }
            TransitionDirection.DIAGONAL_FROM_BOTTOM_RIGHT -> {
                transitionDiagonalBottomRight(width, height, factor, newTargetFrame, sourceFrame, targetFrame, blendMode)
            }
            else -> {}
        }

        return newTargetFrame
    }

    private fun transitionDiagonalBottomRight(
        width: Int,
        height: Int,
        factor: Double,
        newTargetFrame: XledFrame,
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        blendMode: BlendMode
    ) {
        val n = (min(width, height) * 2 * factor).roundToInt()
        for (y in 0 until n) {
            for (x in 0..y) {
                val xx = max(0, min(width - x, width - 1))
                val yy = max(0, min(height - y + x, height - 1))
                newTargetFrame[xx, yy] = sourceFrame[xx, yy].fade(targetFrame[xx, yy], factor, blendMode)
            }
        }
    }

    private fun transitionDiagonalBottomLeft(
        width: Int,
        height: Int,
        factor: Double,
        newTargetFrame: XledFrame,
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        blendMode: BlendMode
    ) {
        val n = (min(width, height) * 2 * factor).roundToInt()
        for (y in 0 until n) {
            for (x in 0..y) {
                val xx = min(x, width - 1)
                val yy = max(0, min(height - y + x, height - 1))
                newTargetFrame[xx, yy] = sourceFrame[xx, yy].fade(targetFrame[xx, yy], factor, blendMode)
            }
        }
    }

    private fun transitionDiagonalTopRight(
        width: Int,
        height: Int,
        factor: Double,
        newTargetFrame: XledFrame,
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        blendMode: BlendMode
    ) {
        val n = (min(width, height) * 2 * factor).roundToInt()
        for (y in 0 until n) {
            for (x in 0..y) {
                val xx = max(0, min(width - x, width - 1))
                val yy = min(y - x, height - 1)
                newTargetFrame[xx, yy] = sourceFrame[xx, yy].fade(targetFrame[xx, yy], factor, blendMode)
            }
        }
    }

    private fun transitionDiagonalTopLeft(
        width: Int,
        height: Int,
        factor: Double,
        newTargetFrame: XledFrame,
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        blendMode: BlendMode
    ) {
        val n = (min(width, height) * 2 * factor).roundToInt()
        for (y in 0 until n) {
            for (x in 0..y) {
                val xx = min(x, width - 1)
                val yy = min(y - x, height - 1)
                newTargetFrame[xx, yy] = sourceFrame[xx, yy].fade(targetFrame[xx, yy], factor, blendMode)
            }
        }
    }

    private fun transitionDownUp(
        height: Int,
        factor: Double,
        width: Int,
        newTargetFrame: XledFrame,
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        blendMode: BlendMode
    ) {
        val n = (height * factor).roundToInt()
        for (x in 0 until width) {
            for (y in 1..n) {
                newTargetFrame[x, height - y] =
                    sourceFrame[x, height - y].fade(targetFrame[x, height - y], factor, blendMode)
            }
        }
    }

    private fun transitionUpDown(
        height: Int,
        factor: Double,
        width: Int,
        newTargetFrame: XledFrame,
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        blendMode: BlendMode
    ) {
        val n = (height * factor).roundToInt()
        for (x in 0 until width) {
            for (y in 0 until n) {
                newTargetFrame[x, y] = sourceFrame[x, y].fade(targetFrame[x, y], factor, blendMode)
            }
        }
    }

    private fun transitionRightLeft(
        width: Int,
        factor: Double,
        height: Int,
        newTargetFrame: XledFrame,
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        blendMode: BlendMode
    ) {
        val n = (width * factor).roundToInt()
        for (y in 0 until height) {
            for (x in 1..n) {
                newTargetFrame[width - x, y] =
                    sourceFrame[width - x, y].fade(targetFrame[width - x, y], factor, blendMode)
            }
        }
    }

    private fun transitionLeftRight(
        width: Int,
        factor: Double,
        height: Int,
        newTargetFrame: XledFrame,
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        blendMode: BlendMode
    ) {
        val n = (width * factor).roundToInt()
        for (y in 0 until height) {
            for (x in 0 until n) {
                newTargetFrame[x, y] = sourceFrame[x, y].fade(targetFrame[x, y], factor, blendMode)
            }
        }
    }
}
