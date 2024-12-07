@file:OptIn(ExperimentalStdlibApi::class)

package de.visualdigits.kotlin.twinkly.model.color

import de.visualdigits.kotlin.twinkly.model.color.BlendMode.entries


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
