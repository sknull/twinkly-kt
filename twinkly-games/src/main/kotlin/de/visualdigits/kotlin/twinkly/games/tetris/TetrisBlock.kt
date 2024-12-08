package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.TwinklyColor
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame

open class TetrisBlock(
    width: Int,
    height: Int,
    initialColor: TwinklyColor<*>,
    var opaquePixels: List<Pair<Int, Int>> = listOf()
) : XledFrame(width, height) {

    protected var transparentPixels: List<Pair<Int, Int>> = listOf()

    init {
        opaquePixels.forEach { (px, py) ->
            this[px, py] = initialColor
        }
        calculateTransparentPixels()
    }

    private fun calculateTransparentPixels(): TetrisBlock {
        val tp = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (this[x, y].isBlack()) {
                    tp.add(Pair(x, y))
                }
            }
        }
        transparentPixels = tp.toList()

        return this
    }

    override fun rotateRight(): TetrisBlock {
        val newFrame = TetrisBlock(height, width, initialColor)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[height - y - 1, x] = this[x, y]
            }
        }

        return newFrame.calculateTransparentPixels()
    }

    override fun rotateLeft(): TetrisBlock {
        val newFrame = TetrisBlock(height, width, initialColor)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[y, width - x - 1] = this[x, y]
            }
        }
        return newFrame.calculateTransparentPixels()
    }

    override fun rotate180(): TetrisBlock {
        val newFrame = TetrisBlock(width, height, initialColor)
        for (y in 0 until height) {
            for (x in 0 until width) {
                newFrame[width - x - 1, height - y - 1] = this[x, y]
            }
        }

        return newFrame.calculateTransparentPixels()
    }

    fun draw(frame: XledFrame, x: Int, y: Int) {
        for (by in 0 until height) {
            for (bx in 0 until width) {
                if (!this[bx, by].isBlack()) {
                    frame[x + bx, y + by] = this[bx, by]
                }
            }
        }
    }

//    fun opaquePixels(): List<Pair<Int, Int>> {
//        return opaquePixels.filter { pixel -> pixel.second ==  }
//    }
}
