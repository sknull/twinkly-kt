package de.visualdigits.kotlin.twinkly.games.tetris

import de.visualdigits.kotlin.twinkly.model.color.RGBColor

class BlockI : TetrisBlock(
    width = 1,
    height = 4,
    initialColor = RGBColor(0, 255, 255),
    opaquePixels = listOf(
        Pair(0, 0),
        Pair(0, 1),
        Pair(0, 2),
        Pair(0, 3),
    )
)
