package de.visualdigits.kotlin.minim

/**
 * A Rectangular window function
 * A Rectangular window is equivalent to using no window at all.
 *
 * @author Damien Di Fede
 * @author Corban Brook
 * @see [The Rectangular Window](http://en.wikipedia.org/wiki/Window_function.Rectangular_window)
 */
class RectangularWindow {

    private var length = 0

    /**
     * Apply the window function to a sample buffer.
     *
     * @param samples a sample buffer
     */
    fun apply(samples: DoubleArray) {
        length = samples.size
        for (n in samples.indices) {
            samples[n] = samples[n].times(1.0)
        }
    }

    /**
     * Apply the window to a portion of this sample buffer,
     * given an offset from the beginning of the buffer
     * and the number of samples to be windowed.
     *
     * @param samples float[]: the array of samples to apply the window to
     * @param offset  int: the index in the array to begin windowing
     * @param length  int: how many samples to apply the window to
     */
    fun apply(samples: DoubleArray, offset: Int, length: Int) {
        this.length = length
        for (n in offset until offset + length) {
            samples[n] = samples[n].times(1.0)
        }
    }
}
