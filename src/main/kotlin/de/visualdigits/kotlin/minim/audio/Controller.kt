package de.visualdigits.kotlin.minim.audio

import org.slf4j.LoggerFactory
import javax.sound.sampled.BooleanControl
import javax.sound.sampled.Control
import javax.sound.sampled.FloatControl

/**
 * `Controller` is the base class of all Minim classes that deal
 * with audio I/O. It provides control over the underlying `DataLine`,
 * which is a low-level JavaSound class that talks directly to the audio
 * hardware of the computer. This means that you can make changes to the audio
 * without having to manipulate the samples directly. The downside to this is
 * that when outputting sound to the system (such as with an
 * `AudioOutput`), these changes will not be present in the
 * samples made available to your program.
 *
 *
 * The [.volume], [.gain], [.pan], and
 * [.balance] methods return objects of type `FloatControl`,
 * which is a class defined by the JavaSound API. A `FloatControl`
 * represents a control of a line that holds a `float` value. This
 * value has an associated maximum and minimum value (such as between -1 and 1
 * for pan), and also a unit type (such as dB for gain). You should refer to the
 * [FloatControl
 * Javadoc](http://java.sun.com/j2se/1.5.0/docs/api/javax/sound/sampled/FloatControl.html) for the full description of the methods available.
 *
 *
 * Not all controls are available on all objects. Before calling the methods
 * mentioned above, you should call
 * [.hasControl] with the control type
 * you want to use. Alternatively, you can use the `get` and
 * `set` methods, which will simply do nothing if the control you
 * are trying to manipulate is not available.
 *
 * @author Damien Di Fede
 */
open class Controller(
    /**
     * @return an array of all available controls
     * Returns an array of all the available `Control`s for the
     * `DataLine` being controlled. You can use this if you want to
     * access the controls directly, rather than using the convenience methods
     * provided by this class.
     */
    val controls: Array<Control>
) {

    private val log = LoggerFactory.getLogger(Controller::class.java)

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

    /**
     * Constructs a `Controller` for the given `Line`.
     *
     * @param cntrls an array of Controls that this Controller will manipulate
     * 
     */
    init {
        isShiftingBalance = isShiftingPan
        isShiftingGain = isShiftingBalance
        isShiftingVolume = isShiftingGain
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

    // a small class to interpolate a value over time
    internal inner class ValueShifter(private val vstart: Float, private val vend: Float, t: Int) {
        private val tstart: Float
        private val tend: Float

        init {
            tstart = System.currentTimeMillis().toInt().toFloat()
            tend = tstart + t
        }

        fun value(): Float {
            val millis = System.currentTimeMillis().toInt()
            val norm = (millis - tstart) / (tend - tstart)
            val range = vend - vstart
            return vstart + range * norm
        }

        fun done(): Boolean {
            return System.currentTimeMillis().toInt() > tend
        }
    }

    /**
     * Prints the available controls and their ranges to the console. Not all
     * Controllers have all of the controls available on them so this is a way to find
     * out what is available.
     */
    fun printControls() {
        if (controls.size > 0) {
            println("Available controls are:")
            for (i in controls.indices) {
                val type = controls[i].type
                print("  $type")
                if (type === FloatControl.Type.VOLUME || type === FloatControl.Type.MASTER_GAIN || type === FloatControl.Type.BALANCE || type === FloatControl.Type.PAN) {
                    val fc = controls[i] as FloatControl
                    var shiftSupported = "does"
                    if (fc.updatePeriod == -1) {
                        shiftSupported = "doesn't"
                    }
                    println(
                        ", which has a range of " + fc.maximum + " to "
                                + fc.minimum + " and " + shiftSupported
                                + " support shifting."
                    )
                }
                else {
                    println()
                }
            }
        }
        else {
            println("There are no controls available.")
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
        for (i in controls.indices) {
            if (controls[i].type == type) {
                return true
            }
        }
        return false
    }

    fun getControl(type: Control.Type): Control? {
        for (i in controls.indices) {
            if (controls[i].type == type) {
                return controls[i]
            }
        }
        return null
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
        setValue(BooleanControl.Type.MUTE, true)
    }

    /**
     * Unmutes the sound.
     */
    fun unmute() {
        setValue(BooleanControl.Type.MUTE, false)
    }

    /**
     * Returns true if the sound is muted.
     *
     * @return the current mute state
     */
    fun isMuted(): Boolean {
        return getValue(BooleanControl.Type.MUTE)
    }

    private fun getValue(type: BooleanControl.Type): Boolean {
        var v = false
        if (hasControl(type)) {
            val c = getControl(type) as BooleanControl?
            v = c!!.value
        }
        else {
            log.error("$type is not supported.")
        }
        return v
    }

    private fun getValue(type: FloatControl.Type): Float {
        var v = 0f
        if (hasControl(type)) {
            val c = getControl(type) as FloatControl?
            v = c!!.value
        }
        else {
            log.error("$type is not supported.")
        }
        return v
    }

    private fun setValue(type: BooleanControl.Type, v: Boolean) {
        if (hasControl(type)) {
            val c = getControl(type) as BooleanControl?
            c!!.value = v
        }
        else {
            log.error("$type is not supported.")
        }
    }

    private fun setValue(type: FloatControl.Type, v: Float) {
        var v = v
        if (hasControl(type)) {
            val c = getControl(type) as FloatControl?
            if (v > c!!.maximum) {
                v = c.maximum
            }
            else if (v < c.minimum) {
                v = c.minimum
            }
            c.setValue(v)
        }
        else {
            log.error("$type is not supported.")
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
    fun getVolume(): Float {
        return getValue(FloatControl.Type.VOLUME)
    }

    /**
     * Sets the volume. If a volume control is not available,
     * this does nothing.
     *
     * @param value float: the new value for the volume, usually in the range [0,1].
     * @shortdesc Sets the volume.
     *      *      *      */
    fun setVolume(value: Float) {
        setValue(FloatControl.Type.VOLUME, value)
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
    fun getGain(): Float {
        return getValue(FloatControl.Type.MASTER_GAIN)
    }

    /**
     * Sets the gain. If a gain control is not available,
     * this does nothing.
     *
     * @param value float: the new value for the gain, expressed in decibels.
     * @shortdesc Sets the gain.
     *      *      *      */
    fun setGain(value: Float) {
        setValue(FloatControl.Type.MASTER_GAIN, value)
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
    fun getBalance(): Float {
        return getValue(FloatControl.Type.BALANCE)
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
        setValue(FloatControl.Type.BALANCE, value)
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
    fun getPan(): Float {
        return getValue(FloatControl.Type.PAN)
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
        setValue(FloatControl.Type.PAN, value)
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
}

