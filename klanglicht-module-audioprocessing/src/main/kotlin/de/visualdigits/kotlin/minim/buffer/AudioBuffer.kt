package de.visualdigits.kotlin.minim.buffer

import org.slf4j.LoggerFactory
import kotlin.math.sqrt

/**
 * `MAudioBuffer` encapsulates a sample buffer of floats. All Minim
 * classes that give you access to audio samples do so with an
 * `MAudioBuffer`. The underlying array is not immutable and this
 * class has a number of methods for reading and writing to that array. It is
 * even possible to be given a direct handle on the array to process it as you
 * wish.
 *
 * @author Damien Di Fede
 */
class AudioBuffer(sampleCount: Int) {

    private val log = LoggerFactory.getLogger(AudioBuffer::class.java)

    private var samples: FloatArray

    /**
     * Constructs and MAudioBuffer that is `bufferSize` samples long.
     *
     */
    init {
        samples = FloatArray(sampleCount) { 0.0F }
    }

    @Synchronized
    fun size(): Int {
        return samples.size
    }

    @Synchronized
    operator fun get(i: Int): Float {
        return samples[i]
    }

    @Synchronized
    operator fun get(i: Float): Float {
        val lowSamp = i.toInt()
        val hiSamp = lowSamp + 1
        if (hiSamp == samples.size) {
            return samples[lowSamp]
        }
        val lerp = i - lowSamp
        return (samples[lowSamp]) + lerp * ((samples[hiSamp]) - (samples[lowSamp]))
    }

    @Synchronized
    fun set(buffer: FloatArray) {
        if (buffer.size != samples.size) {
           log.error(
                    "MAudioBuffer.set: passed array (" + buffer.size + ") " +
                            "must be the same length (" + samples.size + ") as this MAudioBuffer."
                )
        }
        else {
            samples = buffer
        }
    }

    /**
     * Mixes the two float arrays and puts the result in this buffer. The
     * passed arrays must be the same length as this buffer. If they are not, an
     * error will be reported and nothing will be done. The mixing function is:
     *
     * @param b1 the first buffer
     * @param b2 the second buffer
     */
    @Synchronized
    fun mix(b1: FloatArray, b2: FloatArray) {
        if (((b1.size != b2.size) || (b1.size != samples.size))
        ) {
            log.error("MAudioBuffer.mix: The two passed buffers must be the same size as this MAudioBuffer.")
        }
        else {
            for (i in samples.indices) {
                samples[i] = (b1[i] + (b2[i])) / 2
            }
        }
    }

    /**
     * Sets all of the values in this buffer to zero.
     */
    @Synchronized
    fun clear() {
        samples = FloatArray(samples.size) { 0.0F }
    }

    @Synchronized
    fun level(): Double {
        return sqrt(samples.average())
    }

    @Synchronized
    fun toArray(): FloatArray {
        val ret = FloatArray(samples.size) { 0.0F }
        System.arraycopy(samples, 0, ret, 0, samples.size)
        return ret
    }
}

