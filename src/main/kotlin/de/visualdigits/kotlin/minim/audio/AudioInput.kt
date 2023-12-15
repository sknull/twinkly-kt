package de.visualdigits.kotlin.minim.audio

import de.visualdigits.kotlin.minim.Minim
import de.visualdigits.kotlin.minim.buffer.AudioBuffer
import de.visualdigits.kotlin.minim.buffer.StereoBuffer
import org.slf4j.LoggerFactory
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.BooleanControl
import javax.sound.sampled.Control
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
class AudioInput(
    stream: AudioResource,
    val out: AudioOutput
) : Recordable, AutoCloseable {

    private val log = LoggerFactory.getLogger(AudioInput::class.java)

    // the starting value for shifting
    private var vshifter: ValueShifter? = null
    private var gshifter: ValueShifter? = null
    private var bshifter: ValueShifter? = null
    private var pshifter: ValueShifter? = null

    /**
     * Returns true if the volume is currently shifting.
     * If no volume control is available this method returns false.
     *
     * @return true if shifting, false otherwise
     *      *      *      */
    private var isShiftingVolume: Boolean

    /**
     * Returns true if the gain is currently shifting.
     * If no gain control is available this method returns false.
     *
     * @return true if shifting, false otherwise
     *      *      *      */
    private var isShiftingGain: Boolean

    /**
     * Returns true if the balance is currently shifting.
     * If no gain control is available this method returns false.
     *
     * @return true if shifting, false otherwise
     *      *      *      */
    private var isShiftingBalance: Boolean

    /**
     * Returns true if the pan is currently shifting.
     * If no gain control is available this method returns false.
     *
     * @return true if shifting, false otherwise
     *      *      *      */
    private var isShiftingPan = false

    private val controlsMap: Map<Control.Type, Control>


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
     *      *      */
    val left: AudioBuffer

    /**
     * The AudioBuffer containing the right channel samples. If this is a mono
     * sound, `right` contains the same samples as
     * `left`.
     *
     *      *      */
    val right: AudioBuffer

    /**
     * The AudioBuffer containing the mix of the left and right channels. If this is
     * a mono sound, `mix` contains the same
     * samples as `left`.
     *
     *      *      */
    val mix: AudioBuffer

    /**
     * Returns whether or not this AudioInput is monitoring.
     * In other words, whether you will hear in your speakers
     * the audio coming into the input.
     *
     * @return boolean: true if monitoring is on
     *      *      *      *      */
    private var isMonitoring = false
    private var mStream: AudioResource

    /**
     * Constructs an `AudioInput` that uses `out` to read
     * samples from `stream`. The samples from `stream`
     * can be accessed by through the interface provided by `AudioSource`.
     */
    init {
        isShiftingBalance = isShiftingPan
        isShiftingGain = isShiftingBalance
        isShiftingVolume = isShiftingGain
        controlsMap = out.getControls().map { Pair(it.type, it) }.toMap()

        // we gots a buffer for users to poll
        buffer = StereoBuffer(
            out.getFormat().channels,
            out.bufferSize(), this
        )
        left = buffer.left
        right = buffer.right
        mix = buffer.mix

        // we gots a signal splitter that we'll add any listeners the user wants
        splitter = SignalSplitter(out.getFormat(), out.bufferSize())
        // we stick our buffer in the signal splitter because we can only set
        // one
        // listener on the stream
        splitter.addListener(buffer)
        // and there it goes.
        out.setAudioListener(splitter)
        out.open()

        out.setAudioStream(stream)
        stream.open()
        disableMonitoring()
        mStream = stream
    }

    // for line reading/writing classes to alert the controller
    // that a new buffer has been read/written
    fun update() {
        if (isShiftingVolume) {
            setVolume(vshifter!!.value())
            if (vshifter!!.done()) {
                isShiftingVolume = false
            }
        }
        if (isShiftingGain) {
            setGain(gshifter!!.value())
            if (gshifter!!.done()) {
                isShiftingGain = false
            }
        }
        if (isShiftingBalance) {
            setBalance(bshifter!!.value())
            if (bshifter!!.done()) {
                isShiftingBalance = false
            }
        }
        if (isShiftingPan) {
            setPan(pshifter!!.value())
            if (pshifter!!.done()) {
                isShiftingPan = false
            }
        }
    }

    /**
     * @param type the Control.Type to query for
     * @return true if the control is available
     * Returns whether or not the particular control type is supported by this Controller
     * @see .VOLUME
     *
     * @see .GAIN
     *
     * @see .BALANCE
     *
     * @see .PAN
     *
     * @see .SAMPLE_RATE
     *
     * @see .MUTE
     */
    fun hasControl(type: Control.Type): Boolean {
        return controlsMap.containsKey(type)
    }

    fun getControl(type: Control.Type): Control? {
        return controlsMap[type]
    }

    /**
     * @return the volume control
     * Gets the volume control for the `Line`, if it exists. You
     * should check for the availability of a volume control by using
     * [.hasControl] before calling this
     * method.
     */
    fun volume(): FloatControl? {
        return getControl(FloatControl.Type.VOLUME) as FloatControl?
    }

    /**
     * @return the gain control
     * Gets the gain control for the `Line`, if it exists. You
     * should check for the availability of a gain control by using
     * [.hasControl] before calling this
     * method.
     */
    fun gain(): FloatControl? {
        return getControl(FloatControl.Type.MASTER_GAIN) as FloatControl?
    }

    /**
     * @return the balance control
     * Gets the balance control for the `Line`, if it exists. You
     * should check for the availability of a balance control by using
     * [.hasControl] before calling this
     * method.
     */
    fun balance(): FloatControl? {
        return getControl(FloatControl.Type.BALANCE) as FloatControl?
    }

    /**
     * @return the pan control
     * Gets the pan control for the `Line`, if it exists. You should
     * check for the availability of a pan control by using
     * [.hasControl] before calling this
     * method.
     */
    fun pan(): FloatControl? {
        return getControl(FloatControl.Type.PAN) as FloatControl?
    }

    /**
     * Mutes the sound.
     */
    fun mute() {
        setBooleanValue(BooleanControl.Type.MUTE, true)
    }

    /**
     * Unmutes the sound.
     */
    fun unmute() {
        setBooleanValue(BooleanControl.Type.MUTE, false)
    }

    /**
     * Returns true if the sound is muted.
     *
     * @return the current mute state
     */
    fun isMuted(): Boolean? {
        return getBooleanValue(BooleanControl.Type.MUTE)
    }

    private fun getBooleanValue(type: BooleanControl.Type): Boolean? {
        return getControl(type)?.let { (it as BooleanControl).value }
    }

    private fun setBooleanValue(type: BooleanControl.Type, value: Boolean) {
        getControl(type)?.let { control ->
            (control as BooleanControl).value = value
        }
    }

    private fun getFloatValue(type: FloatControl.Type): Float? {
        return getControl(type)?.let { (it as FloatControl).value }
    }

    private fun setFloatValue(type: FloatControl.Type, value: Float) {
        getControl(type)?.let { control ->
            val fc = (control as FloatControl)
            if (value >= fc.minimum && value <= fc.maximum) {
                fc.value = value
            }
        }
    }

    /**
     * Returns the current volume. If a volume control is not available, this
     * returns 0. Note that the volume is not the same thing as the
     * `level()` of an AudioBuffer!
     *
     * @return the current volume or zero if a volume control is unavailable
     * @shortdesc Returns the current volume.
     *      *      */
    fun getVolume(): Float? {
        return getFloatValue(FloatControl.Type.VOLUME)
    }

    /**
     * Sets the volume. If a volume control is not available,
     * this does nothing.
     *
     * @param value float: the new value for the volume, usually in the range [0,1].
     * @shortdesc Sets the volume.
     *      *      *      */
    fun setVolume(value: Float) {
        setFloatValue(FloatControl.Type.VOLUME, value)
    }

    /**
     * Transitions the volume from one value to another.
     *
     * @param from   float: the starting volume
     * @param to     float: the ending volume
     * @param millis int: the length of the transition in milliseconds
     *      *      *      */
    fun shiftVolume(from: Float, to: Float, millis: Int) {
        if (hasControl(FloatControl.Type.VOLUME)) {
            setVolume(from)
            vshifter = ValueShifter(from, to, millis)
            isShiftingVolume = true
        }
    }

    /**
     * Returns the current gain. If a gain control is not available, this returns
     * 0. Note that the gain is not the same thing as the `level()`
     * of an AudioBuffer! Gain describes the current volume of the sound in
     * decibels, which is a logarithmic, rather than linear, scale. A gain
     * of 0dB means the sound is not being amplified or attenuated. Negative
     * gain values will reduce the volume of the sound, and positive values
     * will increase it.
     *
     *
     * See: [http://wikipedia.org/wiki/Decibel](http://wikipedia.org/wiki/Decibel)
     *
     * @return float: the current gain or zero if a gain control is unavailable.
     * the gain is expressed in decibels.
     * @shortdesc Returns the current gain.
     *      *      *      */
    fun getGain(): Float? {
        return getFloatValue(FloatControl.Type.MASTER_GAIN)
    }

    /**
     * Sets the gain. If a gain control is not available,
     * this does nothing.
     *
     * @param value float: the new value for the gain, expressed in decibels.
     * @shortdesc Sets the gain.
     *      *      *      */
    fun setGain(value: Float) {
        setFloatValue(FloatControl.Type.MASTER_GAIN, value)
    }

    /**
     * Transitions the gain from one value to another.
     *
     * @param from   float: the starting gain
     * @param to     float: the ending gain
     * @param millis int: the length of the transition in milliseconds
     *      *      *      */
    fun shiftGain(from: Float, to: Float, millis: Int) {
        if (hasControl(FloatControl.Type.MASTER_GAIN)) {
            setGain(from)
            gshifter = ValueShifter(from, to, millis)
            isShiftingGain = true
        }
    }

    /**
     * Returns the current balance. This will be in the range [-1, 1].
     * Usually balance will only be available for stereo audio sources,
     * because it describes how much attenuation should be applied to
     * the left and right channels.
     * If a balance control is not available, this will do nothing.
     *
     * @return float: the current balance or zero if a balance control is unavailable
     * @shortdesc Returns the current balance.
     *      *      *      */
    fun getBalance(): Float? {
        return getFloatValue(FloatControl.Type.BALANCE)
    }

    /**
     * Sets the balance.
     * The value should be in the range [-1, 1].
     * If a balance control is not available, this will do nothing.
     *
     * @param value float: the new value for the balance
     * @shortdesc Sets the balance.
     *      *      *      */
    fun setBalance(value: Float) {
        setFloatValue(FloatControl.Type.BALANCE, value)
    }

    /**
     * Transitions the balance from one value to another.
     *
     * @param from   float: the starting balance
     * @param to     float: the ending balance
     * @param millis int: the length of the transition in milliseconds
     *      *      *      */
    fun shiftBalance(from: Float, to: Float, millis: Int) {
        if (hasControl(FloatControl.Type.BALANCE)) {
            setBalance(from)
            bshifter = ValueShifter(from, to, millis)
            isShiftingBalance = true
        }
    }

    /**
     * Returns the current pan.
     * Usually pan will be only be available on mono audio sources because
     * it describes a mono signal's position in a stereo field.
     * This will be in the range [-1, 1], where -1 will place the sound
     * only in the left speaker and 1 will place the sound only in the right speaker.
     *
     * @return float: the current pan or zero if a pan control is unavailable
     * @shortdesc Returns the current pan.
     *      *      *      */
    fun getPan(): Float? {
        return getFloatValue(FloatControl.Type.PAN)
    }

    /**
     * Sets the pan.
     * The provided value should be in the range [-1, 1].
     * If a pan control is not present, this does nothing.
     *
     * @param value float: the new value for the pan
     * @shortdesc Sets the pan.
     *      *      *      */
    fun setPan(value: Float) {
        setFloatValue(FloatControl.Type.PAN, value)
    }

    /**
     * Transitions the pan from one value to another.
     *
     * @param from   float: the starting pan
     * @param to     float: the ending pan
     * @param millis int: the length of the transition in milliseconds
     *      *      *      */
    fun shiftPan(from: Float, to: Float, millis: Int) {
        if (hasControl(FloatControl.Type.PAN)) {
            setPan(from)
            pshifter = ValueShifter(from, to, millis)
            isShiftingPan = true
        }
    }

    /**
     * Add an AudioListener to this sound generating object,
     * which will have its samples method called every time
     * this object generates a new buffer of samples.
     *
     * @param listener the AudioListener that will listen to this
     * @shortdesc Add an AudioListener to this sound generating object.
     *      *      */
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
     *      */
    override fun bufferSize(): Int {
        return out.bufferSize()
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
     *      */
    override fun getFormat(): AudioFormat {
        return out.getFormat()
    }

    /**
     * Removes an AudioListener that was previously
     * added to this sound object.
     *
     * @param listener the AudioListener that should stop listening to this
     *      *      */
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
        return out.getFormat().channels
    }

    /**
     * Returns the sample rate of this sound object.
     *
     * @return the sample rate of this sound object.
     */
    override fun sampleRate(): Double {
        return out.getFormat().sampleRate.toDouble()
    }

    override fun close() {
        log.debug("Closing $this")
        out.close()

        // if we have a parent, tell them to stop tracking us
        // so that we can get garbage collected
        parent?.removeSource(this)
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

