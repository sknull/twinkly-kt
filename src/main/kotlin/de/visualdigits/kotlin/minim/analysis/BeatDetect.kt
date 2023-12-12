package de.visualdigits.kotlin.minim.analysis

import de.visualdigits.kotlin.minim.buffer.AudioBuffer
import org.slf4j.LoggerFactory
import java.util.stream.IntStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


class BeatDetect(
    private val algorithm: DetectMode = DetectMode.SOUND_ENERGY,
    private val sampleRate: Float = 44100.0F,
    private val timeSize: Int = 1024,
    private var sensitivity: Int = 10,
    /**
     * time incremented after every call to detect, to know how many milliseconds of audio we have processed so far.
     * this value is used as part of the the sensitivity implementation
     */
    private var detectTimeMillis: Long = 0L
) {

    private val log = LoggerFactory.getLogger(BeatDetect::class.java)

    // for circular buffer support
    private var insertAt = 0

    /**
     * In sound energy mode this returns true when a beat has been detected. In
     * frequency energy mode this always returns false.
     *
     * @return boolean: true if a beat has been detected.
     */
    // vars for sEnergy
    var isOnset = false
    private var eBuffer: FloatArray = floatArrayOf()
    private var dBuffer: FloatArray = floatArrayOf()

    // a millisecond timer used to prevent reporting onsets until the sensitivity threshold has been reached
    // see the sEnergy method
    private var sensitivityTimer: Long = 0

    // vars for fEnergy
    private var fIsOnset: BooleanArray = booleanArrayOf()
    private var spect: FFT = FFT(timeSize, sampleRate)
    private var feBuffer: Array<FloatArray> = arrayOf()
    private var fdBuffer: Array<FloatArray> = arrayOf()
    private var fTimer: LongArray = longArrayOf()

    init {
        initSEResources()
        initFEResources()
    }

    private fun initSEResources() {
        isOnset = false
        eBuffer = FloatArray((sampleRate / timeSize).roundToInt()) { 0.0F }
        dBuffer = FloatArray((sampleRate / timeSize).roundToInt()) { 0.0F }
        sensitivityTimer = 0
        insertAt = 0
    }

    private fun initFEResources() {
        spect = FFT(timeSize, sampleRate)
        spect.logAverages(60, 3)
        val numAvg = spect.avgSize()
        fIsOnset = BooleanArray(numAvg)
        feBuffer = Array(numAvg) { FloatArray((sampleRate / timeSize).roundToInt()) { 0.0F } }
        fdBuffer = Array(numAvg) { FloatArray((sampleRate / timeSize).roundToInt()) { 0.0F } }
        fTimer = LongArray(numAvg)
        IntStream.range(0, fTimer.size).forEach { i: Int -> fTimer[i] = 0 }
        insertAt = 0
    }

    /**
     * Analyze the samples in `buffer`.
     * This is a cumulative process, so you must call this function every playable.
     *
     * @param buffer AudioBuffer: the buffer to analyze.
     */
    fun detect(buffer: AudioBuffer) {
        val buffer1 = buffer.toArray()
        when (algorithm) {
            DetectMode.SOUND_ENERGY -> sEnergy(buffer1)
            DetectMode.FREQ_ENERGY -> fEnergy(buffer1)
        }
    }

    /**
     * In frequency energy mode this returns the number of frequency bands
     * currently being used. In sound energy mode this always returns 0.
     *
     * @return int: the length of the FFT's averages array
     */
    fun detectSize(): Int {
        return if (algorithm == DetectMode.FREQ_ENERGY) {
            spect.avgSize()
        }
        else 0
    }

    /**
     * Returns the center frequency of the i<sup>th</sup> frequency band.
     * In sound energy mode this always returns 0.
     *
     * @param i int: which detect band you want the center frequency of.
     * @return Float: the center frequency of the i<sup>th</sup> frequency band
     */
    fun getDetectCenterFrequency(i: Int): Float {
        return if (algorithm == DetectMode.FREQ_ENERGY) {
            spect.getAverageCenterFrequency(i)
        }
        else 0.0F
    }

    /**
     * In frequency energy mode this returns true when a beat has been detect in
     * the `i<sup>th</sup>` frequency band. In sound energy mode
     * this always returns false.
     *
     * @param i int: the frequency band to query
     * @return boolean: true if a beat has been detected in the requested band
     */
    fun isOnset(i: Int): Boolean {
        return if (algorithm == DetectMode.SOUND_ENERGY) {
            false
        }
        else fIsOnset[i]
    }

    /**
     * In frequency energy mode this returns true if a beat corresponding to the
     * frequency range of a kick drum has been detected. This has been tuned to
     * work well with dance / techno music and may not perform well with other
     * styles of music. In sound energy mode this always returns false.
     *
     * @return boolean: true if a kick drum beat has been detected
     */
    fun isKick(): Boolean {
        if (algorithm == DetectMode.SOUND_ENERGY) {
            return false
        }
        val upper = min(6, spect.avgSize())
        return isRange(1, upper, 2)
    }

    /**
     * In frequency energy mode this returns true if a beat corresponding to the
     * frequency range of a snare drum has been detected. This has been tuned to
     * work well with dance / techno music and may not perform well with other
     * styles of music. In sound energy mode this always returns false.
     *
     * @return boolean: true if a snare drum beat has been detected
     */
    fun isSnare(): Boolean {
        if (algorithm == DetectMode.SOUND_ENERGY) {
            return false
        }
        val lower = min(8, spect.avgSize())
        val upper = spect.avgSize() - 1
        val thresh = (upper - lower) / 3 + 1
        return isRange(lower, upper, thresh)
    }

    /**
     * In frequency energy mode this returns true if a beat corresponding to the
     * frequency range of a hi hat has been detected. This has been tuned to work
     * well with dance / techno music and may not perform well with other styles
     * of music. In sound energy mode this always returns false.
     *
     * @return boolean: true if a hi hat beat has been detected
     */
    fun isHiHat(): Boolean {
        if (algorithm == DetectMode.SOUND_ENERGY) {
            return false
        }
        val lower = max((spect.avgSize() - 7), 0)
        val upper = spect.avgSize() - 1
        return isRange(lower, upper, 1)
    }

    /**
     * In frequency energy mode this returns true if at least
     * `threshold` bands of the bands included in the range
     * `[low, high]` have registered a beat. In sound energy mode
     * this always returns false.
     *
     * @param low       int: the index of the lower band
     * @param high      int: the index of the higher band
     * @param threshold int: the smallest number of bands in the range
     * `[low, high]` that need to have registered a beat
     * for this to return true
     * @return boolean: true if at least `threshold` bands of the bands
     * included in the range `[low, high]` have registered a
     * beat
     */
    fun isRange(low: Int, high: Int, threshold: Int): Boolean {
        if (algorithm == DetectMode.SOUND_ENERGY) {
            return false
        }
        var num = 0
        for (i in low until high + 1) {
            if (isOnset(i)) {
                num++
            }
        }
        return num >= threshold
    }

    private fun sEnergy(samples: FloatArray) {
        // compute the energy level
        var level = 0.0F
        for (i in samples.indices) {
            val fl = samples[i]
            level += fl * fl
        }
        level /= samples.size
        level = sqrt(level)
        val instant = level * 100
        // compute the average local energy
        val e = average(eBuffer)
        // compute the variance of the energies in eBuffer
        val v = variance(eBuffer, e)
        // compute C using a linear digression of C with V
        val c = -0.0025714F * v + 1.5142857F
        // filter negative values
        val diff = max((instant - c * e), 0.0F)
        // find the average of only the positive values in dBuffer
        val dAvg = specAverage(dBuffer)
        // filter negative values
        val diff2 = max((diff - dAvg), 0.0F)
        // report false if it's been less than 'sensitivity'
        // milliseconds since the last true value
        if (detectTimeMillis - sensitivityTimer < sensitivity) {
            isOnset = false
        }
        else if (diff2 > 0 && instant > 2) {
            isOnset = true
            sensitivityTimer = detectTimeMillis
        }
        else {
            isOnset = false
        }
        eBuffer[insertAt] = instant
        dBuffer[insertAt] = diff
        insertAt++
        if (insertAt == eBuffer.size) {
            insertAt = 0
        }
        // advance the current time by the number of milliseconds this buffer represents
        detectTimeMillis += (samples.size / sampleRate * 1000).toLong()
    }

    private fun fEnergy(sample: FloatArray) {
        spect.forward(sample)
        var instant: Float
        var e: Float
        var v: Float
        var c: Float
        var diff: Float
        var dAvg: Float
        var diff2: Float
        for (i in feBuffer.indices) {
            instant = spect.getAvg(i)
            e = average(feBuffer[i])
            v = variance(feBuffer[i], e)
            c = -0.0025714f * v + 1.5142857f
            diff = max((instant - c * e), 0.0F)
            dAvg = specAverage(fdBuffer[i])
            diff2 = max((diff - dAvg), 0.0F)
            if (detectTimeMillis - fTimer[i] < sensitivity) {
                fIsOnset[i] = false
            }
            else if (diff2 > 0) {
                fIsOnset[i] = true
                fTimer[i] = detectTimeMillis
            }
            else {
                fIsOnset[i] = false
            }
            feBuffer[i][insertAt] = instant
            fdBuffer[i][insertAt] = diff
        }
        insertAt++
        if (insertAt == feBuffer[0].size) {
            insertAt = 0
        }
        // advance the current time by the number of milliseconds this buffer represents
        detectTimeMillis += (sample.size / sampleRate * 1000).toLong()
    }

    private fun average(arr: FloatArray): Float {
        var avg = 0.0F
        for (v in arr) {
            avg += v
        }
        avg /= arr.size
        return avg
    }

    private fun specAverage(arr: FloatArray): Float {
        var avg = 0.0F
        var num = 0.0F
        for (i in arr.indices) {
            if (arr[i] > 0) {
                avg += arr[i]
                num++
            }
        }
        if (num > 0) {
            avg /= num
        }
        return avg
    }

    private fun variance(arr: FloatArray, value: Float): Float {
        var v = 0.0F
        for (i in arr.indices) {
            v += (arr[i] - value).pow(2.0F)
        }
        v /= arr.size
        return v
    }
}

