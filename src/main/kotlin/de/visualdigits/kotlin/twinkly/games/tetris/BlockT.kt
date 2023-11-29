package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockT() : TetrisBlock(
    width = 3,
    height = 2,
    initialColor = RGBColor(50, 0, 255),
    pixelsToCheck = listOf(Pair(0, 1), Pair(1, 2), Pair(2, 1))
) {

    init {
        frame[0][1] = RGBColor(0, 0, 0, 0)
        frame[2][1] = RGBColor(0, 0, 0, 0)
    }
}
