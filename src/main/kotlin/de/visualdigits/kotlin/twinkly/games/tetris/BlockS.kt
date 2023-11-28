package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockS() : TetrisBlock(3, 2, RGBColor(0, 200, 0)) {

    init {
        frame[0][0] = RGBColor(0, 0, 0, 0)
        frame[2][1] = RGBColor(0, 0, 0, 0)
    }
}
