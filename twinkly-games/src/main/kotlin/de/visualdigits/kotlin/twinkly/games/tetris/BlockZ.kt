package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockZ : TetrisBlock(
    width = 3,
    height = 2,
    initialColor = RGBColor(255, 0, 0),
    opaquePixels = listOf(
        Pair(0, 0),
        Pair(1, 0),
        Pair(1, 1),
        Pair(2, 1),
    )
)
