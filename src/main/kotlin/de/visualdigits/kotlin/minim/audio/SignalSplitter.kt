package de.visualdigits.kotlin.minim.audio

import java.util.*
import javax.sound.sampled.AudioFormat

/**
 * A `SignalSplitter` acts exactly like a headphone splitter.
 * When you pass it audio with the `samples` method, it echoes that
 * audio out to all of its listeners, giving each their own copy of the audio.
 * In other words, changes that the listeners make to the float arrays
 * they receive from a `SignalSplitter` will not be reflected in
 * the arrays you pass to `samples`. `SignalSplitter` is
 * fully `synchronized` so that listeners cannot be added and
 * removed while it is in the midst transmitting.
 *
 *
 * This class is also useful for performing offline rendering of audio.
 *
 * @author Damien Di Fede
 *  */
class SignalSplitter(
    /**
     * Returns the format of this recordable audio.
     *
     * @return the format of the audio
     */
    private val f: AudioFormat,
    private val bs: Int
) : Recordable, AudioListener {
    
    private val listeners: Vector<AudioListener> = Vector(5)

    /**
     * The buffer size this was constructed with. Arrays passed to generate should be the same length.
     *
     * @return int: the expected buffer size for generate calls
     */
    override fun bufferSize(): Int {
        return bs
    }

    /**
     * Returns either Minim.MONO or Minim.STEREO
     *
     * @return Minim.MONO if this is mono, Minim.STEREO if this is stereo
     */
    override fun type(): Int {
        return f.channels
    }

    /**
     * Adds a listener who will be notified each time this receives
     * or creates a new buffer of samples. If the listener has already
     * been added, it will not be added again.
     *
     * @param listener the listener to add
     *      */
    @Synchronized
    override fun addListener(listener: AudioListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    /**
     * Removes the listener from the list of listeners.
     *
     * @param listener the listener to remove
     *      */
    @Synchronized
    override fun removeListener(listener: AudioListener) {
        listeners.remove(listener)
    }

    override fun getFormat(): AudioFormat = f

    /**
     * Called by the audio object this is attached to when that object has new samples,
     * but can also be called directly when doing offline rendering.
     *
     * @param sampL a float[] buffer containing the left channel of a STEREO sound stream
     * @param sampR a float[] buffer containing the right channel of a STEREO sound stream
     *      *      */
    @Synchronized
    override fun samples(sampL: DoubleArray, sampR: DoubleArray?) {
        for (i in listeners.indices) {
            val al = listeners[i]
            val copyL = DoubleArray(sampL.size) { 0.0 }
            System.arraycopy(sampL, 0, copyL, 0, copyL.size)
            if (sampR != null) {
                val copyR = DoubleArray(sampR.size) { 0.0 }
                System.arraycopy(sampR, 0, copyR, 0, copyR.size)
                al.samples(copyL, copyR)
            } else {
                al.samples(copyL)
            }
        }
    }

    /**
     * Returns the sample rate of the audio.
     *
     * @return the sample rate of the audio
     */
    override fun sampleRate(): Float {
        return f.sampleRate
    }
}

