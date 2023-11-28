package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockZ() : TetrisBlock(3, 2, RGBColor(255, 0, 0)) {

    init {
        frame[2][0] = RGBColor(0, 0, 0, 0)
        frame[0][1] = RGBColor(0, 0, 0, 0)
    }
}
