package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockL : TetrisBlock(
    width = 2,
    height = 3,
    initialColor = RGBColor(255, 50, 0),
    opaquePixels = listOf(
        Pair(0, 0),
        Pair(0, 1),
        Pair(0, 2),
        Pair(1, 2),
    )
)
