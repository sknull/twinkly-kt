package de.visualdigits.kotlin.minim.audio

/**
 * An `AudioListener` can be used to monitor `Recordable`
 * objects such as `AudioPlayer`, `AudioOutput`, and `AudioInput`.
 * Each time a `Recordable` object receives a new sample buffer
 * from the audio system, or generates a new sample buffer at the request of the
 * audio system, it passes a copy of this buffer to its listeners. You can
 * implement this interface if you want to receive samples in a callback fashion,
 * rather than using an object's `AudioBuffer`s to access them. You
 * add an `AudioListener` to a `Recordable` by calling
 * the addListener method. When you want to stop receiving samples you call the
 * removeListener method.
 *
 * @author Damien Di Fede
 *  *  *  *  *  */
interface AudioListener {

    /**
     * Called by the `Recordable` object this is attached to
     * when that object has new samples.
     *
     * @param sampL a float[] buffer containing the left channel of a STEREO sound stream
     * @param sampR a float[] buffer containing the right channel of a STEREO sound stream
     *      */
    fun samples(
        sampL: FloatArray,
        sampR: FloatArray? = null
    )
}

