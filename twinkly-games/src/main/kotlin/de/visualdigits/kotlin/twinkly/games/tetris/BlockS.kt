package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockS : TetrisBlock(
    width = 3,
    height = 2,
    initialColor = RGBColor(0, 200, 0),
    opaquePixels = listOf(
        Pair(1, 0),
        Pair(2, 0),
        Pair(0, 1),
        Pair(1, 1),
    )
)
