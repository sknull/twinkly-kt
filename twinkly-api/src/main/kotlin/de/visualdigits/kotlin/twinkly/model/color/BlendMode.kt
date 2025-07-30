@file:OptIn(ExperimentalStdlibApi::class)

package de.visualdigits.kotlin.twinkly.model.color


enum class BlendMode {

    ADD,
    SUBTRACT,
    AVERAGE,
    REPLACE
    ;

    companion object {
        fun random(): BlendMode {
            return entries.random()
        }
    }
}
