package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockT : TetrisBlock(
    width = 3,
    height = 2,
    initialColor = RGBColor(50, 0, 255),
    opaquePixels = listOf(
        Pair(0, 0),
        Pair(1, 0),
        Pair(2, 0),
        Pair(1, 1),
    )
)
