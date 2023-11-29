package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockL() : TetrisBlock(
    width = 2,
    height = 3,
    initialColor = RGBColor(255, 50, 0),
    pixelsToCheck = listOf(Pair(0, 3), Pair(1, 3))
) {

    init {
        frame[1][0] = RGBColor(0, 0, 0, 0)
        frame[1][1] = RGBColor(0, 0, 0, 0)
    }
}
