package com.softsynth.math

class BitReverseTable internal constructor(numBits: Int) {
    var reversedBits: IntArray

    init {
        reversedBits = IntArray(1 shl numBits)
        for (i in reversedBits.indices) {
            reversedBits[i] = reverseBits(i, numBits)
        }
    }

    companion object {
        fun reverseBits(index: Int, numBits: Int): Int {
            var index: Int = index
            var i: Int
            var rev: Int
            i = 0.also({ rev = it })
            while (i < numBits) {
                rev = (rev shl 1) or (index and 1)
                index = index shr 1
                i++
            }
            return rev
        }
    }
}
