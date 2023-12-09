package de.visualdigits.kotlin.twinkly.model.frame

import de.visualdigits.kotlin.twinkly.model.xled.XLed

interface Playable {

    fun play(
        xled: XLed,
        loop: Int = -1, // endlesss
        random: Boolean = false
    )

    fun stop()

    fun toByteArray(bytesPerLed: Int): ByteArray
}
