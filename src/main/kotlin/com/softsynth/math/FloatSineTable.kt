package com.softsynth.math

import kotlin.math.sin

class FloatSineTable internal constructor(numBits: Int) {
    var sineValues: FloatArray

    init {
        val len: Int = 1 shl numBits
        sineValues = FloatArray(1 shl numBits)
        for (i in 0 until len) {
            sineValues[i] = sin((i * Math.PI * 2.0) / len).toFloat()
        }
    }
}
