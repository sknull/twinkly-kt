package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockO : TetrisBlock(
    width = 2,
    height = 2,
    initialColor = RGBColor(255, 255, 0),
    opaquePixels = listOf(
        Pair(0, 0),
        Pair(1, 0),
        Pair(0, 1),
        Pair(1, 1),
    )
)
