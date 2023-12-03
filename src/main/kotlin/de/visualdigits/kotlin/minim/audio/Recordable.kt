package de.visualdigits.kotlin.minim.audio

import de.visualdigits.kotlin.minim.audio.AudioListener
import javax.sound.sampled.AudioFormat

/**
 * @author Damien Di Fede
 * A `Recordable` object is one that can provide a program with
 * floating point samples of the audio passing through it. It does this using
 * `AudioListener`s. You add listeners to the `Recordable` and
 * then the `Recordable` will call the appropriate `samples`
 * method of all its listeners when it has a new buffer of samples. It is also
 * possible to query a `Recordable` object for its buffer size, type
 * (mono or stereo), and audio format.
 */
interface Recordable {
    /**
     * Adds a listener who will be notified each time this receives
     * or creates a new buffer of samples. If the listener has already
     * been added, it will not be added again.
     *
     * @param listener the listener to add
     *      */
    fun addListener(listener: AudioListener)

    /**
     * Removes the listener from the list of listeners.
     *
     * @param listener the listener to remove
     *      */
    fun removeListener(listener: AudioListener)

    /**
     * Returns the format of this recordable audio.
     *
     * @return the format of the audio
     */
    fun getFormat(): AudioFormat

    /**
     * Returns either Minim.MONO or Minim.STEREO
     *
     * @return Minim.MONO if this is mono, Minim.STEREO if this is stereo
     */
    fun type(): Int

    /**
     * Returns the buffer size being used by this.
     *
     * @return the buffer size
     */
    fun bufferSize(): Int

    /**
     * Returns the sample rate of the audio.
     *
     * @return the sample rate of the audio
     */
    fun sampleRate(): Float
}

