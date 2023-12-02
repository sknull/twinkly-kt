package de.visualdigits.kotlin.minim

import java.util.Vector
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
 * @example Advanced/OfflineRendering
 */
class SignalSplitter(
    /**
     * Returns the format of this recordable audio.
     *
     * @return the format of the audio
     */
    private val f: AudioFormat,
    private val bs: Int
) : Recordable, AudioListener {
    
    private val listeners: Vector<AudioListener>

    /**
     * Construct a `SignalSplitter` that will receive
     * audio in the given format and in buffers the size of
     * `bufferSize`. Strictly speaking, a `SignalSplitter`
     * doesn't care about either of these things because it does nothing with
     * the samples it receives other than pass them on. But both things are
     * required to fulfill the `Recordable` contract.
     *
     * @param format     the `AudioFormat` of samples that this will receive
     * @param bufferSize the size of the float arrays this will receive
     */
    init {
        listeners = Vector(5)
    }

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
     * @example Advanced/AddAndRemoveAudioListener
     */
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
     * @example Advanced/AddAndRemoveAudioListener
     */
    @Synchronized
    override fun removeListener(listener: AudioListener) {
        listeners.remove(listener)
    }

    override fun getFormat(): AudioFormat = f

    /**
     * Called by the audio object this AudioListener is attached to
     * when that object has new samples, but can also be called directly
     * when doing offline rendering.
     *
     * @param samp a float[] buffer of samples from a MONO sound stream
     * @example Advanced/OfflineRendering
     * @related AudioListener
     */
    @Synchronized
    override fun samples(samp: FloatArray) {
        for (i in listeners.indices) {
            val al = listeners[i]
            val copy = FloatArray(samp.size) { 0.0F }
            System.arraycopy(samp, 0, copy, 0, copy.size)
            al.samples(copy)
        }
    }

    /**
     * Called by the audio object this is attached to when that object has new samples,
     * but can also be called directly when doing offline rendering.
     *
     * @param sampL a float[] buffer containing the left channel of a STEREO sound stream
     * @param sampR a float[] buffer containing the right channel of a STEREO sound stream
     * @example Advanced/OfflineRendering
     * @related AudioListener
     */
    @Synchronized
    override fun samples(sampL: FloatArray, sampR: FloatArray) {
        for (i in listeners.indices) {
            val al = listeners[i]
            val copyL = FloatArray(sampL.size) { 0.0F }
            val copyR = FloatArray(sampR.size) { 0.0F }
            System.arraycopy(sampL, 0, copyL, 0, copyL.size)
            System.arraycopy(sampR, 0, copyR, 0, copyR.size)
            al.samples(copyL, copyR)
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

