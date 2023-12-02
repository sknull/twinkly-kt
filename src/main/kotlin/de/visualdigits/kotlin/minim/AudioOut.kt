package de.visualdigits.kotlin.minim


/**
 * An `AudioSythesizer` is an `AudioStream` that generates
 * sound, rather than reading sound. It uses the attached
 * `AudioSignal` and `AudioEffect` to generate a signal.
 *
 * @author Damien Di Fede
 */
interface AudioOut : AudioResource {
    /**
     * @return the size of the buffer used by this output.
     */
    fun bufferSize(): Int

    /**
     * Sets the AudioStream that this output will use to generate sound.
     *
     */
    fun setAudioStream(stream: AudioStream)

    /**
     * Sets the AudioListener that will have sound broadcasted to it as the
     * output generates.
     *
     */
    fun setAudioListener(listener: AudioListener)
}

