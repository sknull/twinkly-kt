package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockL() : TetrisBlock(2, 3, RGBColor(255, 50, 0)) {

    init {
        frame[1][0] = RGBColor(0, 0, 0, 0)
        frame[1][1] = RGBColor(0, 0, 0, 0)
    }
}
