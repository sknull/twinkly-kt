package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockZ : TetrisBlock(
    width = 3,
    height = 2,
    initialColor = RGBColor(255, 0, 0),
    pixelsToCheck = listOf(Pair(0, 1), Pair(1, 2), Pair(2, 2))
) {

// todo
//    init {
//        frame[2][0] = RGBColor(0, 0, 0, 0)
//        frame[0][1] = RGBColor(0, 0, 0, 0)
//    }
}
