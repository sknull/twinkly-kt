package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockJ() : TetrisBlock(2, 3, RGBColor(0, 0, 255)) {

    init {
        frame[0][0] = RGBColor(0, 0, 0, 0)
        frame[0][1] = RGBColor(0, 0, 0, 0)
    }
}
