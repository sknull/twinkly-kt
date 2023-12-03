package de.visualdigits.kotlin.minim.audio

import org.slf4j.LoggerFactory
import javax.sound.sampled.FloatControl

/**
 * An AudioInput is a connection to the current record source of the computer.
 * How the record source for a computer is set will depend on the soundcard and OS,
 * but typically a user can open a control panel and set the source from there.
 * Unfortunately, there is no way to set the record source from Java.
 *
 *
 * You can obtain an AudioInput from Minim by using one of the getLineIn methods:
 * <pre>
 * // get the default STEREO input
 * AudioInput getLineIn()
 *
 * // specifiy either Minim.MONO or Minim.STEREO for type
 * AudioInput getLineIn(int type)
 *
 * // bufferSize is the size of the left, right,
 * // and mix buffers of the input you get back
 * AudioInput getLineIn(int type, int bufferSize)
 *
 * // sampleRate is a request for an input of a certain sample rate
 * AudioInput getLineIn(int type, int bufferSize, float sampleRate)
 *
 * // bitDepth is a request for an input with a certain bit depth
 * AudioInput getLineIn(int type, int bufferSize, float sampleRate, int bitDepth)
</pre> *
 * In the event that an input doesn't exist with the requested parameters,
 * Minim will spit out an error and return null. In general,
 * you will want to use the first two methods listed above.
 *
 * @author Damien Di Fede
 *  *  */
class AudioInput(stream: AudioStream, out: AudioOutput) : AudioSource(out) {

    private val log = LoggerFactory.getLogger(AudioInput::class.java)

    /**
     * Returns whether or not this AudioInput is monitoring.
     * In other words, whether you will hear in your speakers
     * the audio coming into the input.
     *
     * @return boolean: true if monitoring is on
     *      *      *      *      */
    private var isMonitoring = false
    private var mStream: AudioStream

    /**
     * Constructs an `AudioInput` that uses `out` to read
     * samples from `stream`. The samples from `stream`
     * can be accessed by through the interface provided by `AudioSource`.
     */
    init {
        out.setAudioStream(stream)
        stream.open()
        disableMonitoring()
        mStream = stream
    }

    override fun close() {
        super.close()
        mStream.close()
    }

    /**
     * When monitoring is enabled, you will be able to hear
     * the audio that is coming through the input.
     *
     *      *      *      *      */
        fun enableMonitoring() {
        // make sure we don't make sound
        if (hasControl(FloatControl.Type.VOLUME)) {
            setVolume(1.0F)
            isMonitoring = true
        }
        else if (hasControl(FloatControl.Type.MASTER_GAIN)) {
            setGain(0.0F)
            isMonitoring = true
        }
        else {
            log.error("Monitoring is not available on this AudioInput.")
        }
    }

    /**
     * When monitoring is disabled, you will not hear
     * the audio that is coming through the input,
     * but you will still be able to access the samples
     * in the left, right, and mix buffers. This is
     * default state of an AudioInput and is what
     * you will want if your input is microphone
     * and your output is speakers. Otherwise: feedback.
     *
     * @shortdesc When monitoring is disabled, you will not hear
     * the audio that is coming through the input.
     *      *      *      *      */
        fun disableMonitoring() {
        // make sure we don't make sound
        if (hasControl(FloatControl.Type.VOLUME)) {
            setVolume(0.0F)
        }
        else if (hasControl(FloatControl.Type.MASTER_GAIN)) {
            setGain(-64.0F)
        }
        isMonitoring = false
    }
}

