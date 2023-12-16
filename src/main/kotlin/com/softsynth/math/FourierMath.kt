/*
 * Copyright 2009 Phil Burk, Mobileer Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.softsynth.math

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

//Simple Fast Fourier Transform.
object FourierMath {

    private val MAX_SIZE_LOG_2: Int = 16

    var reverseTables: Array<BitReverseTable?> = arrayOfNulls(MAX_SIZE_LOG_2)
    var sineTables: Array<DoubleSineTable?> = arrayOfNulls(MAX_SIZE_LOG_2)
    var floatSineTables: Array<FloatSineTable?> = arrayOfNulls(MAX_SIZE_LOG_2)

    private fun getDoubleSineTable(n: Int): DoubleArray {
        var sineTable: DoubleSineTable? = sineTables[n]
        if (sineTable == null) {
            sineTable = DoubleSineTable(n)
            sineTables[n] = sineTable
        }
        return sineTable.sineValues
    }

    private fun getFloatSineTable(n: Int): FloatArray {
        var sineTable: FloatSineTable? = floatSineTables[n]
        if (sineTable == null) {
            sineTable = FloatSineTable(n)
            floatSineTables[n] = sineTable
        }
        return sineTable.sineValues
    }

    private fun getReverseTable(n: Int): IntArray {
        var reverseTable: BitReverseTable? = reverseTables[n]
        if (reverseTable == null) {
            reverseTable = BitReverseTable(n)
            reverseTables[n] = reverseTable
        }
        return reverseTable.reversedBits
    }

    /**
     * Calculate the amplitude of the sine wave associated with each bin of a complex FFT result.
     *
     * @param ar
     * @param ai
     * @param magnitudes
     */
    fun calculateMagnitudes(ar: DoubleArray, ai: DoubleArray, magnitudes: DoubleArray) {
        for (i in magnitudes.indices) {
            magnitudes[i] = sqrt((ar[i] * ar[i]) + (ai[i] * ai[i]))
        }
    }

    fun calculateDb(magnitudes: DoubleArray, db: DoubleArray) {
        val normalizeOffset = 20.0 * log10(magnitudes.size * 2.0.pow(8) / 2.0)
        for (i in 0 until magnitudes.size) {
            db[i] = 20.0 * log10(magnitudes[i]) - normalizeOffset
        }
    }

    /**
     * Calculate the amplitude of the sine wave associated with each bin of a complex FFT result.
     *
     * @param ar
     * @param ai
     * @param magnitudes
     */
    fun calculateMagnitudes(ar: FloatArray, ai: FloatArray, magnitudes: FloatArray) {
        for (i in magnitudes.indices) {
            magnitudes[i] = sqrt(((ar[i] * ar[i]) + (ai[i] * ai[i])).toDouble()).toFloat()
        }
    }

    fun transform(sign: Int, n: Int, ar: DoubleArray, ai: DoubleArray) {
        val scale: Double = if ((sign > 0)) (2.0 / n) else (0.5)
        val numBits: Int = numBits(n)
        val reverseTable: IntArray = getReverseTable(numBits)
        val sineTable: DoubleArray = getDoubleSineTable(numBits)
        val mask: Int = n - 1
        val cosineOffset: Int = n / 4 // phase offset between cos and sin
        var i: Int
        var j: Int
        i = 0
        while (i < n) {
            j = reverseTable[i]
            if (j >= i) {
                val tempr: Double = ar!![j] * scale
                val tempi: Double = ai!![j] * scale
                ar[j] = ar[i] * scale
                ai[j] = ai[i] * scale
                ar[i] = tempr
                ai[i] = tempi
            }
            i++
        }
        var mmax: Int
        var stride: Int
        val numerator: Int = sign * n
        mmax = 1
        stride = 2 * mmax
        while (mmax < n) {
            var phase: Int = 0
            val phaseIncrement: Int = numerator / (2 * mmax)
            for (m in 0 until mmax) {
                val wr: Double = sineTable[(phase + cosineOffset) and mask] // cosine
                val wi: Double = sineTable[phase]
                i = m
                while (i < n) {
                    j = i + mmax
                    val tr: Double = (wr * ar!![j]) - (wi * ai!![j])
                    val ti: Double = (wr * ai[j]) + (wi * ar[j])
                    ar[j] = ar[i] - tr
                    ai[j] = ai[i] - ti
                    ar[i] += tr
                    ai[i] += ti
                    i += stride
                }
                phase = (phase + phaseIncrement) and mask
            }
            mmax = stride
            mmax = stride
            stride = 2 * mmax
        }
    }

    fun transform(sign: Int, n: Int, ar: FloatArray, ai: FloatArray) {
        val scale: Float = if ((sign > 0)) (2.0f / n) else (0.5f)
        val numBits: Int = numBits(n)
        val reverseTable: IntArray = getReverseTable(numBits)
        val sineTable: FloatArray = getFloatSineTable(numBits)
        val mask: Int = n - 1
        val cosineOffset: Int = n / 4 // phase offset between cos and sin
        var i: Int
        var j: Int
        i = 0
        while (i < n) {
            j = reverseTable[i]
            if (j >= i) {
                val tempr: Float = ar[j] * scale
                val tempi: Float = ai[j] * scale
                ar[j] = ar[i] * scale
                ai[j] = ai[i] * scale
                ar[i] = tempr
                ai[i] = tempi
            }
            i++
        }
        var mmax: Int
        var stride: Int
        val numerator: Int = sign * n
        mmax = 1
        stride = 2 * mmax
        while (mmax < n) {
            var phase: Int = 0
            val phaseIncrement: Int = numerator / (2 * mmax)
            for (m in 0 until mmax) {
                val wr: Float = sineTable[(phase + cosineOffset) and mask] // cosine
                val wi: Float = sineTable[phase]
                i = m
                while (i < n) {
                    j = i + mmax
                    val tr: Float = (wr * ar[j]) - (wi * ai[j])
                    val ti: Float = (wr * ai[j]) + (wi * ar[j])
                    ar[j] = ar[i] - tr
                    ai[j] = ai[i] - ti
                    ar[i] += tr
                    ai[i] += ti
                    i += stride
                }
                phase = (phase + phaseIncrement) and mask
            }
            mmax = stride
            mmax = stride
            stride = 2 * mmax
        }
    }

    /**
     * Calculate log2(n)
     *
     * @param powerOf2 must be a power of two, for example 512 or 1024
     * @return for example, 9 for an input value of 512
     */
    fun numBits(powerOf2: Int): Int {
        var powerOf2: Int = powerOf2
        var i: Int
        assert(
            ((powerOf2 and (powerOf2 - 1)) == 0) // is it a power of 2?
        )
        i = -1
        while (powerOf2 > 0) {
            powerOf2 = powerOf2 shr 1
            i++
        }
        return i
    }

    /**
     * Calculate an FFT in place, modifying the input arrays.
     *
     * @param n
     * @param ar
     * @param ai
     */
    fun fft(n: Int, ar: DoubleArray, ai: DoubleArray) {
        transform(1, n, ar, ai) // TODO -1 or 1
    }

    /**
     * Calculate an inverse FFT in place, modifying the input arrays.
     *
     * @param n
     * @param ar
     * @param ai
     */
    fun ifft(n: Int, ar: DoubleArray, ai: DoubleArray) {
        transform(-1, n, ar, ai) // TODO -1 or 1
    }

}
