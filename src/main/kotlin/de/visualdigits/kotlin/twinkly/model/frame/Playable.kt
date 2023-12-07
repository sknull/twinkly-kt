package de.visualdigits.kotlin.twinkly.model.frame

import de.visualdigits.kotlin.twinkly.model.xled.XLed

interface Playable {

    fun play(
        xled: XLed,
        frameDelay: Long = 1000,
        sequenceDelay: Long = 1000,
        frameLoop: Int = -1, // endlesss
        sequenceLoop: Int = 0, // no loop
        random: Boolean = false
    )

    fun toByteArray(bytesPerLed: Int): ByteArray
}
