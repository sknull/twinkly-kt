package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockJ() : TetrisBlock(
    width = 2,
    height = 3,
    initialColor = RGBColor(0, 0, 255),
    pixelsToCheck = listOf(Pair(0, 3), Pair(1, 3))
) {

    init {
        frame[0][0] = RGBColor(0, 0, 0, 0)
        frame[0][1] = RGBColor(0, 0, 0, 0)
    }
}
