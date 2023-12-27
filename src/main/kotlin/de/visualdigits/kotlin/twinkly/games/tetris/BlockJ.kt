package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockJ : TetrisBlock(
    width = 2,
    height = 3,
    initialColor = RGBColor(0, 0, 255),
    opaquePixels = listOf(
        Pair(1, 0),
        Pair(1, 1),
        Pair(0, 2),
        Pair(1, 2),
    )
)
