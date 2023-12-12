package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockS : TetrisBlock(
    width = 3,
    height = 2,
    initialColor = RGBColor(0, 200, 0),
    pixelsToCheck = listOf(Pair(0, 2), Pair(1, 2), Pair(2, 1))
) {

    init {
        frame[0][0] = RGBColor(0, 0, 0, 0)
        frame[2][1] = RGBColor(0, 0, 0, 0)
    }
}
