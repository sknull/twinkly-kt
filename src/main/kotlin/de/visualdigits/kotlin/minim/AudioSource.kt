package de.visualdigits.kotlin.minim

import org.slf4j.LoggerFactory
import javax.sound.sampled.AudioFormat


open class AudioSource(val stream: AudioOut) : Controller(stream.getControls()), Recordable {

    private val log = LoggerFactory.getLogger(MinimServiceProvider::class.java)

    // the instance of Minim that created us, if one did.
    var parent: Minim? = null

    // the signal splitter used to manage listeners to the source
    // our stereobuffer will be the first in the list
    private val splitter: SignalSplitter

    // the StereoBuffer that will subscribe to synth
    private val buffer: StereoBuffer

    /**
     * The AudioBuffer containing the left channel samples. If this is a mono
     * sound, it contains the single channel of audio.
     *
     * @example Basics/PlayAFile
     * @related AudioBuffer
     */
    val left: AudioBuffer

    /**
     * The AudioBuffer containing the right channel samples. If this is a mono
     * sound, `right` contains the same samples as
     * `left`.
     *
     * @example Basics/PlayAFile
     * @related AudioBuffer
     */
    val right: AudioBuffer

    /**
     * The AudioBuffer containing the mix of the left and right channels. If this is
     * a mono sound, `mix` contains the same
     * samples as `left`.
     *
     * @example Basics/PlayAFile
     * @related AudioBuffer
     */
    val mix: AudioBuffer

    /**
     * Constructs an `AudioSource` that will subscribe to the samples
     * in `stream`. It is expected that the stream is using a
     * `DataLine` for playback. If it is not, calls to
     * `Controller`'s methods will result in a
     * `NullPointerException`.
     *
     * @param istream the `AudioStream` to subscribe to and wrap
     * @invisible
     */
    init {

        // we gots a buffer for users to poll
        buffer = StereoBuffer(
            stream.getFormat().channels,
            stream.bufferSize(), this
        )
        left = buffer.left
        right = buffer.right
        mix = buffer.mix

        // we gots a signal splitter that we'll add any listeners the user wants
        splitter = SignalSplitter(stream.getFormat(), stream.bufferSize())
        // we stick our buffer in the signal splitter because we can only set
        // one
        // listener on the stream
        splitter.addListener(buffer)
        // and there it goes.
        stream.setAudioListener(splitter)
        stream.open()
    }

    /**
     * Closes this source, making it unavailable.
     *
     * @invisible
     */
    open fun close() {
        log.debug("Closing $this")
        stream.close()

        // if we have a parent, tell them to stop tracking us
        // so that we can get garbage collected
        parent?.removeSource(this)
    }

    /**
     * Add an AudioListener to this sound generating object,
     * which will have its samples method called every time
     * this object generates a new buffer of samples.
     *
     * @param listener the AudioListener that will listen to this
     * @shortdesc Add an AudioListener to this sound generating object.
     * @example Advanced/AddAndRemoveAudioListener
     * @related AudioListener
     */
    override fun addListener(listener: AudioListener) {
        splitter.addListener(listener)
    }

    /**
     * The internal buffer size of this sound object.
     * The left, right, and mix AudioBuffers of this object
     * will be this large, and sample buffers passed to
     * AudioListeners added to this object will be this large.
     *
     * @return int: the internal buffer size of this sound object, in sample frames.
     * @shortdesc The internal buffer size of this sound object.
     * @example Basics/PlayAFile
     */
    override fun bufferSize(): Int {
        return stream.bufferSize()
    }

    /**
     * Returns an AudioFormat object that describes the audio properties
     * of this sound generating object. This is often useful information
     * when doing sound analysis or some synthesis, but typically you
     * will not need to know about the specific format.
     *
     * @return an AudioFormat describing this sound object.
     * @shortdesc Returns AudioFormat object that describes the audio properties
     * of this sound generating object.
     * @example Advanced/GetAudioFormat
     */
    override fun getFormat(): AudioFormat {
        return stream.getFormat()
    }

    /**
     * Removes an AudioListener that was previously
     * added to this sound object.
     *
     * @param listener the AudioListener that should stop listening to this
     * @example Advanced/AddAndRemoveAudioListener
     * @related AudioListener
     */
    override fun removeListener(listener: AudioListener) {
        splitter.removeListener(listener)
    }

    /**
     * The type is an int describing the number of channels
     * this sound object has.
     *
     * @return Minim.MONO if this is mono, Minim.STEREO if this is stereo
     */
    override fun type(): Int {
        return stream.getFormat().channels
    }

    /**
     * Returns the sample rate of this sound object.
     *
     * @return the sample rate of this sound object.
     */
    override fun sampleRate(): Float {
        return stream.getFormat().sampleRate
    }
}

