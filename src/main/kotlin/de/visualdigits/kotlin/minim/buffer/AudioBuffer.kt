package de.visualdigits.kotlin.minim.buffer

/**
 * An `AudioBuffer` is a buffer of floating point samples
 * corresponding to a single channel of streaming audio. It is readonly, but you
 * can obtain a copy of the samples in the buffer by using the `toArray` method.
 * In fact, when drawing a waveform, you should use the `toArray` method
 * rather than iterating over the buffer itself because it is possible that the samples
 * in the buffer will be replaced with new ones between calls to the `get` method,
 * which results in a waveform that appears to have discontinuities in it.
 *
 * @author Damien Di Fede
 */
interface AudioBuffer {

    /**
     * Returns the length of the buffer.
     *
     * @return int: the number of samples in the buffer
     *      */
    fun size(): Int

    /**
     * Gets the `i<sup>th</sup>` sample in the buffer. This method
     * does not do bounds checking, so it may throw an exception.
     *
     * @param i int: the index of the sample you want to get
     * @return float: the `i<sup>th</sup>` sample
     *      *      */
    operator fun get(i: Int): Float

    /**
     * Gets the current level of the buffer. It is calculated as the
     * root-mean-square of all the samples in the buffer.
     *
     * @return float: the RMS amplitude of the buffer
     *      *      */
    fun level(): Float

    /**
     * Returns the samples in the buffer in a new float array.
     * Modifying the samples in the returned array will not change
     * the samples in the buffer.
     *
     * @return float[]: a new float array containing the buffer's samples
     *      */
    fun toArray(): FloatArray
}
