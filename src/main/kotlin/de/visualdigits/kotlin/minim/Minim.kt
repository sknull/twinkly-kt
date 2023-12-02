package de.visualdigits.kotlin.minim

import org.slf4j.LoggerFactory

class Minim {

    private val log = LoggerFactory.getLogger(Minim::class.java)

    private val mimp: MinimServiceProvider = MinimServiceProvider()

    // we keep track of all the resources we are asked to create
    // so that when shutting down the library, users can simply call stop(),
    // and don't have to call close() on all of the things they've created.
    // in the event that they *do* call close() on resource we've created,
    // it will be removed from this list.
    private val sources = ArrayList<AudioSource>()

    // and unfortunately we have to track stream separately
    private val streams = ArrayList<AudioStream>()

    /**
     * An AudioInput is used when you want to monitor the active audio input
     * of the computer. On a laptop, for instance, this will typically be
     * the built-in microphone. On a desktop it might be the line-in
     * port on the soundcard. The default values are for a stereo input
     * with a 1024 sample buffer (ie the size of left, right, and mix
     * buffers), sample rate of 44100 and bit depth of 16. Generally
     * speaking, you will not want to specify these things, but it's
     * there if you need it.
     *
     * @return an AudioInput that reads from the active audio input of the soundcard
     * @see .getLineIn
     */
    fun getLineIn(): AudioInput? {
        return getLineIn(AudioInputType.STEREO)
    }

    /**
     * Gets either a MONO or STEREO [AudioInput].
     *
     * @param type Minim.MONO or Minim.STEREO
     * @return an `AudioInput` with the requested type, a 1024 sample
     * buffer, a sample rate of 44100 and a bit depth of 16
     * @see .getLineIn
     */
    fun getLineIn(type: AudioInputType): AudioInput? {
        return getLineIn(type, 1024, 44100f, 16)
    }

    /**
     * Gets an [AudioInput].
     *
     * @param type       Minim.MONO or Minim.STEREO
     * @param bufferSize int: how long you want the `AudioInput`'s sample buffer
     * to be (ie the size of left, right, and mix buffers)
     * @param sampleRate float: the desired sample rate in Hertz (typically 44100)
     * @param bitDepth   int: the desired bit depth (typically 16)
     * @return an `AudioInput` with the requested attributes
     */
    fun getLineIn(type: AudioInputType, bufferSize: Int, sampleRate: Float, bitDepth: Int): AudioInput? {
        var input: AudioInput? = null
        val stream: AudioStream? = mimp.getAudioInput(type, bufferSize, sampleRate, bitDepth)
        if (stream != null) {
            var out: AudioOut? = mimp.getAudioOutput(type.channels, bufferSize, sampleRate, bitDepth)
            // couldn't get an output, the system might not have one available
            // so in that case we provide a basic audio out to the input
            // that will pull samples from it and so forth
            if (out == null) {
                out = BasicAudioOut(stream.getFormat(), bufferSize)
            }
            input = AudioInput(stream, out)
        }
        if (input != null) {
            sources.add(input)
        }
        else {
            log.error("Minim.getLineIn: attempt failed, could not secure an AudioInput.")
        }
        return input
    }

    /**
     * Gets an [AudioInput].
     *
     * @param type       Minim.MONO or Minim.STEREO
     * @param bufferSize int: how long you want the `AudioInput`'s sample buffer
     * to be (ie the size of left, right, and mix buffers)
     * @return an `AudioInput` with the requested attributes, a
     * sample rate of 44100 and a bit depth of 16
     * @see .getLineIn
     */
    fun getLineIn(type: AudioInputType, bufferSize: Int): AudioInput? {
        return getLineIn(type, bufferSize, 44100f, 16)
    }

    /**
     * Gets an [AudioInput].
     *
     * @param type       Minim.MONO or Minim.STEREO
     * @param bufferSize int: how long you want the `AudioInput`'s sample buffer
     * to be (ie the size of left, right, and mix buffers)
     * @param sampleRate float: the desired sample rate in Hertz (typically 44100)
     * @return an `AudioInput` with the requested attributes and a
     * bit depth of 16
     * @see .getLineIn
     */
    fun getLineIn(type: AudioInputType, bufferSize: Int, sampleRate: Float): AudioInput? {
        return getLineIn(type, bufferSize, sampleRate, 16)
    }

    fun removeSource(s: AudioSource?) {
        sources.remove(s)
    }
}
