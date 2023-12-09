package de.visualdigits.kotlin.twinkly.model.frame.transition

enum class TransitionDirection {

    LEFT_RIGHT,
    RIGHT_LEFT,
    UP_DOWN,
    DOWN_UP,
    DIAGONAL_FROM_TOP_LEFT,
    DIAGONAL_FROM_TOP_RIGHT,
    DIAGONAL_FROM_BOTTOM_LEFT,
    DIAGONAL_FROM_BOTTOM_RIGHT
    ;

    companion object {
        fun random(): TransitionDirection {
            return listOf(LEFT_RIGHT, RIGHT_LEFT, UP_DOWN, DOWN_UP).random()
        }
    }
}
