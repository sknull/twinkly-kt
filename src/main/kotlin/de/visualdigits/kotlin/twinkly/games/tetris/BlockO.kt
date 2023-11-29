package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockO() : TetrisBlock(
    width = 2,
    height = 2,
    initialColor = RGBColor(255, 255, 0),
    pixelsToCheck = listOf(Pair(0, 2), Pair(1, 2))
)
