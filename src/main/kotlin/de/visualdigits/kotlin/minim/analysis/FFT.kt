package de.visualdigits.kotlin.minim.analysis

import de.visualdigits.kotlin.minim.buffer.AudioBuffer
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import org.slf4j.LoggerFactory
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


class FFT(
    private val timeSize: Int,
    private val sampleRate: Double,
) {

    private val log = LoggerFactory.getLogger(FFT::class.java)

    private val bandWidth: Double = 2f / timeSize * (sampleRate / 2f)

    private var spectrum = DoubleArray(timeSize) { 0.0 }
    private var db = DoubleArray(timeSize) { 0.0 }
    private var real = DoubleArray(timeSize) { 0.0 }
    private var imag = DoubleArray(timeSize) { 0.0 }
    private var averages = DoubleArray(0) { 0.0 }
    private var whichAverage: AverageType = AverageType.NOAVG
    private var octaves = 0
    private var avgPerOctave = 0

    /**
     * Performs a forward transform on `buffer`.
     *
     * @param buffer AudioBuffer: the buffer to analyze
     */
    fun forward(buffer: AudioBuffer) {
        forward(buffer.toArray().map { it.toDouble() }.toDoubleArray())
    }

    fun forward(buffer: DoubleArray) {
        if (buffer.size != timeSize) {
            log.error("FFT.forward: The length of the passed sample buffer must be equal to timeSize().")
            return
        }

        val fft = FastFourierTransformer(DftNormalization.STANDARD)
        val fftC = fft.transform(buffer, TransformType.FORWARD)
        real = fftC.map { it.real }.toDoubleArray()
        imag = fftC.map { it.imaginary }.toDoubleArray()
        calculateMagnitudes()
        calculateDb()
        calculateAverages()
    }

    private fun calculateMagnitudes() {
        real.zip(imag).withIndex().forEach { entry ->
            val real = entry.value.first
            val imag = entry.value.second
            spectrum[entry.index] = sqrt((real * real + imag * imag))
        }
    }

    private fun calculateDb() {
        val normalizeOffset = 20.0 * log10(timeSize * 2.0.pow(16) / 2.0)
        for (i in 0 until timeSize) {
            db[i] = 20.0 * log10(spectrum[i])// - normalizeOffset
        }
        println(db.toList())
    }

    private fun calculateAverages() {
        when (whichAverage) {
            AverageType.LINAVG -> {
                val avgWidth = spectrum.size / averages.size
                var b = 0
                for (x in 0 until spectrum.size - avgWidth step avgWidth) {
                    averages[b++] = (b until b + avgWidth).map { spectrum[it] }.sum() / avgWidth
                }
//println("#### averages: ${averages.toList()}")
            }

            AverageType.LOGAVG -> {
                for (octave in 0 until octaves) {
                    val (lowFreq, freqStep) = getFreqStep(octave)
                    var f = lowFreq
                    for (j in 0 until avgPerOctave) {
                        val offset = j + octave * avgPerOctave
                        averages[offset] = calcAvg(f, f + freqStep)
                        f += freqStep
                    }
                }
            }

            else -> {}
        }
    }

    /**
     * Calculate the average amplitude of the frequency band bounded by
     * `lowFreq` and `hiFreq`, inclusive.
     *
     * @param lowFreq Double: the lower bound of the band, in Hertz
     * @param hiFreq  Double: the upper bound of the band, in Hertz
     * @return Double: the average of all spectrum values within the bounds
     */
    private fun calcAvg(lowFreq: Double, hiFreq: Double): Double {
        val lowBound = freqToIndex(lowFreq)
        val hiBound = freqToIndex(hiFreq)
        var avg = 0.0
        for (i in lowBound..hiBound) {
            avg += spectrum[i]
        }
        avg /= (hiBound - lowBound + 1)
        return avg
    }

    /**
     * Sets the number of averages used when computing the spectrum and spaces the
     * averages in a linear manner. In other words, each average band will be
     * `specSize() / numAvg` bands wide.
     *
     * @param numAvg int: how many averages to compute
     */
    fun linAverages(numAvg: Int) {
        averages = if (numAvg > spectrum.size / 2) {
            log.error("The number of averages for this transform can be at most " + spectrum.size / 2 + ".")
            return
        } else {
            DoubleArray(numAvg) { 0.0 }
        }
        whichAverage = AverageType.LINAVG
    }

    /**
     * Sets the number of averages used when computing the spectrum based on the
     * minimum bandwidth for an octave and the number of bands per octave. For
     * example, with audio that has a sample rate of 44100 Hz,
     * `logAverages(11, 1)` will result in 12 averages, each
     * corresponding to an octave, the first spanning 0 to 11 Hz. To ensure that
     * each octave band is a full octave, the number of octaves is computed by
     * dividing the Nyquist frequency by two, and then the result of that by two,
     * and so on. This means that the actual bandwidth of the lowest octave may
     * not be exactly the value specified.
     *
     * @param minBandwidth   int: the minimum bandwidth used for an octave, in Hertz.
     * @param bandsPerOctave int: how many bands to split each octave into
     */
    fun logAverages(minBandwidth: Int, bandsPerOctave: Int) {
        var nyq = sampleRate / 2.0
        octaves = 1
        while (nyq > minBandwidth) {
            nyq /= 2
            octaves++
        }
        log.debug("Number of octaves = $octaves")
        avgPerOctave = bandsPerOctave
        averages = DoubleArray(octaves * bandsPerOctave) { 0.0 }
        whichAverage = AverageType.LOGAVG
    }

    /**
     * Returns the length of the time domain signal expected by this transform.
     *
     * @return int: the length of the time domain signal expected by this transform
     */
    fun timeSize(): Int {
        return timeSize
    }

    /**
     * Returns the size of the spectrum created by this transform. In other words,
     * the number of frequency bands produced by this transform. This is typically
     * equal to `timeSize()/2 + 1`, see above for an explanation.
     *
     * @return int: the size of the spectrum
     */
    fun specSize(): Int {
        return spectrum.size
    }

    /**
     * Returns the bandwidth of the requested average band. Using this information
     * and the return value of getAverageCenterFrequency you can determine the
     * lower and upper frequency of any average band.
     *
     * @param averageIndex int: the index of the average you want the bandwidth of
     * @return Double: the bandwidth of the request average band, in Hertz.
     * @see .getAverageCenterFrequency
     */
    fun getAverageBandWidth(averageIndex: Int): Double {
        return when (whichAverage) {
            AverageType.LINAVG -> {
                // an average represents a certain number of bands in the spectrum
                val avgWidth = spectrum.size / averages.size
                avgWidth * bandWidth
            }
            AverageType.LOGAVG -> {
                // which "octave" is this index in?
                val octave = averageIndex / avgPerOctave
                val (_, freqStep) = getFreqStep(octave)
                freqStep
            }
            else -> { 0.0 }
        }
    }

    private fun getFreqStep(octave: Int): Pair<Double, Double> {
        // figure out the low frequency for this octave
        val lowFreq: Double = if (octave == 0) {
            0.0
        } else {
            sampleRate / 2 / 2.0.pow((octaves - octave))
        }
        // and the high frequency for this octave
        val hiFreq: Double = sampleRate / 2 / 2.0.pow((octaves - octave - 1))
        // each average band within the octave will be this big
        val freqStep = (hiFreq - lowFreq) / avgPerOctave
        return Pair(lowFreq, freqStep)
    }

    /**
     * Returns the center frequency of the i<sup>th</sup> average band.
     *
     * @param i int: which average band you want the center frequency of.
     * @return Double: the center frequency of the i<sup>th</sup> average band.
     */
    fun getAverageCenterFrequency(i: Int): Double {
        return when (whichAverage) {
            AverageType.LINAVG -> {
                // an average represents a certain number of bands in the spectrum
                val avgWidth = spectrum.size / averages.size
                // the "center" bin of the average, this is fudgy.
                val centerBinIndex = i * avgWidth + avgWidth / 2
                indexToFreq(centerBinIndex)
            }
            AverageType.LOGAVG -> {
                // which "octave" is this index in?
                val octave = i / avgPerOctave
                // which band within that octave is this?
                val offset = i % avgPerOctave

                val (lowFreq, freqStep) = getFreqStep(octave)

                // figure out the low frequency of the band we care about
                val f = lowFreq + offset * freqStep
                // the center of the band will be the low plus half the width
                f + freqStep / 2
            }
            else -> { 0.0 }
        }
    }

    /**
     * Returns the middle frequency of the i<sup>th</sup> band.
     *
     * @param i int: the index of the band you want to middle frequency of
     * @return Double: the middle frequency, in Hertz, of the requested band of the spectrum
     */
    private fun indexToFreq(i: Int): Double {
        val bw: Double = bandWidth
        // special case: the width of the first bin is half that of the others.
        //               so the center frequency is a quarter of the way.
        if (i == 0) {
            return bw * 0.25F
        }
        // special case: the width of the last bin is half that of the others.
        if (i == spectrum.size - 1) {
            val lastBinBeginFreq = sampleRate / 2 - bw / 2
            val binHalfWidth = bw * 0.25F
            return lastBinBeginFreq + binHalfWidth
        }
        // the center frequency of the ith band is simply i*bw
        // because the first band is half the width of all others.
        // treating it as if it wasn't offsets us to the middle
        // of the band.
        return i * bw
    }

    /**
     * Gets the amplitude of the requested frequency in the spectrum.
     *
     * @param freq Double: the frequency in Hz
     * @return Double: the amplitude of the frequency in the spectrum
     */
    fun getFreq(freq: Double): Double {
        return getBand(freqToIndex(freq))
    }

    /**
     * Returns the amplitude of the requested frequency band.
     *
     * @param i int: the index of a frequency band
     * @return Double: the amplitude of the requested frequency band
     */
    fun getBand(i: Int): Double {
        var q = i
        if (q < 0) {
            q = 0
        }
        if (q > spectrum.size - 1) {
            q = spectrum.size - 1
        }
        return spectrum[q].toDouble()
    }

    /**
     * Returns the index of the frequency band that contains the requested
     * frequency.
     *
     * @param freq Double: the frequency you want the index for (in Hz)
     * @return int: the index of the frequency band that contains freq
     */
    private fun freqToIndex(freq: Double): Int {
        // special case: freq is lower than the bandwidth of spectrum[0]
        if (freq < bandWidth / 2) {
            return 0
        }
        // special case: freq is within the bandwidth of spectrum[spectrum.length - 1]
        if (freq > sampleRate / 2 - bandWidth / 2) {
            return spectrum.size - 1
        }
        // all other cases
        val fraction = freq / sampleRate
        return (timeSize * fraction).roundToInt()
    }

    /**
     * Sets the amplitude of the requested frequency in the spectrum to
     * `a`.
     *
     * @param freq Double: the frequency in Hz
     * @param a    Double: the new amplitude
     */
    fun setFreq(freq: Double, a: Double) {
        setBand(freqToIndex(freq), a)
    }

    private fun setBand(i: Int, a: Double) {
        if (a < 0) {
            log.error("Can't set a frequency band to a negative value.")
            return
        }
        val ad = a.toDouble()
        if (real[i] == 0.0 && imag[i] == 0.0) {
            real[i] = ad
            spectrum[i] = ad
        } else {
            spectrum[i] = spectrum[i] / spectrum[i]
            spectrum[i] = spectrum[i] / spectrum[i]
            spectrum[i] = ad
            spectrum[i] = spectrum[i] * spectrum[i]
            spectrum[i] = spectrum[i] * spectrum[i]
        }
        if (i != 0 && i != timeSize / 2) {
            real[timeSize - i] = real[i]
            imag[timeSize - i] = -imag[i]
        }
    }

    /**
     * Scales the amplitude of the requested frequency by `a`.
     *
     * @param freq Double: the frequency in Hz
     * @param s    Double: the scaling factor
     */
    fun scaleFreq(freq: Double, s: Double) {
        scaleBand(freqToIndex(freq), s)
    }

    private fun scaleBand(i: Int, s: Double) {
        if (s < 0) {
            log.error("Can't scale a frequency band by a negative value.")
            return
        }
        real[i] = real[i].times(s)
        imag[i] = imag[i].times(s)
        spectrum[i] = spectrum[i].times(s)
        if (i != 0 && i != timeSize / 2) {
            real[timeSize - i] = real[i]
            imag[timeSize - i] = -imag[i]
        }
    }

    /**
     * Returns the number of averages currently being calculated.
     *
     * @return int: the length of the averages array
     */
    fun avgSize(): Int {
        return averages.size
    }

    /**
     * Gets the value of the `i<sup>th</sup>` average.
     *
     * @param i int: the average you want the value of
     * @return Double: the value of the requested average band
     */
    fun getAvg(i: Int): Double {
        val ret: Double = if (averages.isNotEmpty()) {
            averages[i]
        } else {
            0.0
        }
        return ret
    }
}

