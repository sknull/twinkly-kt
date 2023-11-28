package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockT() : TetrisBlock(3, 2, RGBColor(50, 0, 255)) {

    init {
        frame[0][1] = RGBColor(0, 0, 0, 0)
        frame[2][1] = RGBColor(0, 0, 0, 0)
    }
}
