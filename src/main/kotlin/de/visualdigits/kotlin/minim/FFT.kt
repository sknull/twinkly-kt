package de.visualdigits.kotlin.minim

import org.slf4j.LoggerFactory
import kotlin.math.pow
import kotlin.math.sqrt


/**
 * FFT stands for Fast Fourier Transform. It is an efficient way to calculate the Complex
 * Discrete Fourier Transform. There is not much to say about this class other than the fact
 * that when you want to analyze the spectrum of an audio buffer you will almost always use
 * this class. One restriction of this class is that the audio buffers you want to analyze
 * must have a length that is a power of two. If you try to construct an FFT with a
 * `timeSize` that is not a power of two, an IllegalArgumentException will be
 * thrown.
 *
 *
 * A Fourier Transform is an algorithm that transforms a signal in the time
 * domain, such as a sample buffer, into a signal in the frequency domain, often
 * called the spectrum. The spectrum does not represent individual frequencies,
 * but actually represents frequency bands centered on particular frequencies.
 * The center frequency of each band is usually expressed as a fraction of the
 * sampling rate of the time domain signal and is equal to the index of the
 * frequency band divided by the total number of bands. The total number of
 * frequency bands is usually equal to the length of the time domain signal, but
 * access is only provided to frequency bands with indices less than half the
 * length, because they correspond to frequencies below the [Nyquist frequency](http://en.wikipedia.org/wiki/Nyquist_frequency).
 * In other words, given a signal of length `N`, there will be
 * `N/2` frequency bands in the spectrum.
 *
 *
 * As an example, if you construct an FFT with a
 * `timeSize` of 1024 and and a `sampleRate` of 44100
 * Hz, then the spectrum will contain values for frequencies below 22010 Hz,
 * which is the Nyquist frequency (half the sample rate). If you ask for the
 * value of band number 5, this will correspond to a frequency band centered on
 * `5/1024 * 44100 = 0.0048828125 * 44100 = 215 Hz`. The width of
 * that frequency band is equal to `2/1024`, expressed as a
 * fraction of the total bandwidth of the spectrum. The total bandwith of the
 * spectrum is equal to the Nyquist frequency, which in this case is 22050, so
 * the bandwidth is equal to about 50 Hz. It is not necessary for you to
 * remember all of these relationships, though it is good to be aware of them.
 * The function `getFreq()` allows you to query the spectrum with a
 * frequency in Hz and the function `bandWidth` will return
 * the bandwidth in Hz of each frequency band in the spectrum.
 *
 *
 * **Usage**
 *
 *
 * A typical usage of the FFT is to analyze a signal so that the
 * frequency spectrum may be represented in some way, typically with vertical
 * lines. You could do this in Processing with the following code, where
 * `audio` is an AudioSource and `fft` is an FFT.
 *
 * <pre>
 * fft.forward(audio.left);
 * for (int i = 0; i &lt; fft.specSize(); i++)
 * {
 * // draw the line for frequency band i, scaling it by 4 so we can see it a bit better
 * line(i, height, i, height - fft.getBand(i) * 4);
 * }
</pre> *
 *
 * **Windowing**
 *
 *
 * Windowing is the process of shaping the audio samples before transforming them
 * to the frequency domain. The Fourier Transform assumes the sample buffer is is a
 * repetitive signal, if a sample buffer is not truly periodic within the measured
 * interval sharp discontinuities may arise that can introduce spectral leakage.
 * Spectral leakage is the speading of signal energy across multiple FFT bins. This
 * "spreading" can drown out narrow band signals and hinder detection.
 *
 *
 *
 * A [windowing function](http://en.wikipedia.org/wiki/Window_function)
 * attempts to reduce spectral leakage by attenuating the measured sample buffer
 * at its end points to eliminate discontinuities. If you call the `window()`
 * function with an appropriate WindowFunction, such as `HammingWindow()`,
 * the sample buffers passed to the object for analysis will be shaped by the current
 * window before being transformed. The result of using a window is to reduce
 * the leakage in the spectrum somewhat.
 *
 *
 * **Averages**
 *
 *
 * FFT also has functions that allow you to request the creation of
 * an average spectrum. An average spectrum is simply a spectrum with fewer
 * bands than the full spectrum where each average band is the average of the
 * amplitudes of some number of contiguous frequency bands in the full spectrum.
 *
 *
 * `linAverages()` allows you to specify the number of averages
 * that you want and will group frequency bands into groups of equal number. So
 * if you have a spectrum with 512 frequency bands and you ask for 64 averages,
 * each average will span 8 bands of the full spectrum.
 *
 *
 * `logAverages()` will group frequency bands by octave and allows
 * you to specify the size of the smallest octave to use (in Hz) and also how
 * many bands to split each octave into. So you might ask for the smallest
 * octave to be 60 Hz and to split each octave into two bands. The result is
 * that the bandwidth of each average is different. One frequency is an octave
 * above another when it's frequency is twice that of the lower frequency. So,
 * 120 Hz is an octave above 60 Hz, 240 Hz is an octave above 120 Hz, and so on.
 * When octaves are split, they are split based on Hz, so if you split the
 * octave 60-120 Hz in half, you will get 60-90Hz and 90-120Hz. You can see how
 * these bandwidths increase as your octave sizes grow. For instance, the last
 * octave will always span `sampleRate/4 - sampleRate/2`, which in
 * the case of audio sampled at 44100 Hz is 11025-22010 Hz. These
 * logarithmically spaced averages are usually much more useful than the full
 * spectrum or the linearly spaced averages because they map more directly to
 * how humans perceive sound.
 *
 *
 * `calcAvg()` allows you to specify the frequency band you want an
 * average calculated for. You might ask for 60-500Hz and this function will
 * group together the bands from the full spectrum that fall into that range and
 * average their amplitudes for you.
 *
 *
 * If you don't want any averages calculated, then you can call
 * `noAverages()`. This will not impact your ability to use
 * `calcAvg()`, it will merely prevent the object from calculating
 * an average array every time you use `forward()`.
 *
 *
 * **Inverse Transform**
 *
 *
 * FFT also supports taking the inverse transform of a spectrum.
 * This means that a frequency spectrum will be transformed into a time domain
 * signal and placed in a provided sample buffer. The length of the time domain
 * signal will be `timeSize()` long. The `set` and
 * `scale` functions allow you the ability to shape the spectrum
 * already stored in the object before taking the inverse transform. You might
 * use these to filter frequencies in a spectrum or modify it in some other way.
 * A Fourier Transform is an algorithm that transforms a signal in the time
 * domain, such as a sample buffer, into a signal in the frequency domain, often
 * called the spectrum. The spectrum does not represent individual frequencies,
 * but actually represents frequency bands centered on particular frequencies.
 * The center frequency of each band is usually expressed as a fraction of the
 * sampling rate of the time domain signal and is equal to the index of the
 * frequency band divided by the total number of bands. The total number of
 * frequency bands is usually equal to the length of the time domain signal, but
 * access is only provided to frequency bands with indices less than half the
 * length, because they correspond to frequencies below the [Nyquist frequency](http://en.wikipedia.org/wiki/Nyquist_frequency).
 * In other words, given a signal of length `N`, there will be
 * `N/2` frequency bands in the spectrum.
 *
 *
 * As an example, if you construct a FourierTransform with a
 * `timeSize` of 1024 and and a `sampleRate` of 44100
 * Hz, then the spectrum will contain values for frequencies below 22010 Hz,
 * which is the Nyquist frequency (half the sample rate). If you ask for the
 * value of band number 5, this will correspond to a frequency band centered on
 * `5/1024 * 44100 = 0.0048828125 * 44100 = 215 Hz`. The width of
 * that frequency band is equal to `2/1024`, expressed as a
 * fraction of the total bandwidth of the spectrum. The total bandwith of the
 * spectrum is equal to the Nyquist frequency, which in this case is 22050, so
 * the bandwidth is equal to about 50 Hz. It is not necessary for you to
 * remember all of these relationships, though it is good to be aware of them.
 * The function `getFreq()` allows you to query the spectrum with a
 * frequency in Hz and the function `bandWidth` will return
 * the bandwidth in Hz of each frequency band in the spectrum.
 *
 *
 * **Usage**
 *
 *
 * A typical usage of a FourierTransform is to analyze a signal so that the
 * frequency spectrum may be represented in some way, typically with vertical
 * lines. You could do this in Processing with the following code, where
 * `audio` is an AudioSource and `fft` is an FFT (one
 * of the derived classes of FourierTransform).
 *
 * <pre>
 * fft.forward(audio.left);
 * for (int i = 0; i &lt; fft.specSize(); i++)
 * {
 * // draw the line for frequency band i, scaling it by 4 so we can see it a bit better
 * line(i, height, i, height - fft.getBand(i) * 4);
 * }
</pre> *
 *
 * **Windowing**
 *
 *
 * Windowing is the process of shaping the audio samples before transforming them
 * to the frequency domain. The Fourier Transform assumes the sample buffer is is a
 * repetitive signal, if a sample buffer is not truly periodic within the measured
 * interval sharp discontinuities may arise that can introduce spectral leakage.
 * Spectral leakage is the speading of signal energy across multiple FFT bins. This
 * "spreading" can drown out narrow band signals and hinder detection.
 *
 *
 *
 * A [windowing function](http://en.wikipedia.org/wiki/Window_function)
 * attempts to reduce spectral leakage by attenuating the measured sample buffer
 * at its end points to eliminate discontinuities. If you call the `window()`
 * function with an appropriate WindowFunction, such as `HammingWindow()`,
 * the sample buffers passed to the object for analysis will be shaped by the current
 * window before being transformed. The result of using a window is to reduce
 * the leakage in the spectrum somewhat.
 *
 *
 * **Averages**
 *
 *
 * FourierTransform also has functions that allow you to request the creation of
 * an average spectrum. An average spectrum is simply a spectrum with fewer
 * bands than the full spectrum where each average band is the average of the
 * amplitudes of some number of contiguous frequency bands in the full spectrum.
 *
 *
 * `linAverages()` allows you to specify the number of averages
 * that you want and will group frequency bands into groups of equal number. So
 * if you have a spectrum with 512 frequency bands and you ask for 64 averages,
 * each average will span 8 bands of the full spectrum.
 *
 *
 * `logAverages()` will group frequency bands by octave and allows
 * you to specify the size of the smallest octave to use (in Hz) and also how
 * many bands to split each octave into. So you might ask for the smallest
 * octave to be 60 Hz and to split each octave into two bands. The result is
 * that the bandwidth of each average is different. One frequency is an octave
 * above another when it's frequency is twice that of the lower frequency. So,
 * 120 Hz is an octave above 60 Hz, 240 Hz is an octave above 120 Hz, and so on.
 * When octaves are split, they are split based on Hz, so if you split the
 * octave 60-120 Hz in half, you will get 60-90Hz and 90-120Hz. You can see how
 * these bandwidths increase as your octave sizes grow. For instance, the last
 * octave will always span `sampleRate/4 - sampleRate/2`, which in
 * the case of audio sampled at 44100 Hz is 11025-22010 Hz. These
 * logarithmically spaced averages are usually much more useful than the full
 * spectrum or the linearly spaced averages because they map more directly to
 * how humans perceive sound.
 *
 *
 * `calcAvg()` allows you to specify the frequency band you want an
 * average calculated for. You might ask for 60-500Hz and this function will
 * group together the bands from the full spectrum that fall into that range and
 * average their amplitudes for you.
 *
 *
 * If you don't want any averages calculated, then you can call
 * `noAverages()`. This will not impact your ability to use
 * `calcAvg()`, it will merely prevent the object from calculating
 * an average array every time you use `forward()`.
 *
 *
 * **Inverse Transform**
 *
 *
 * FourierTransform also supports taking the inverse transform of a spectrum.
 * This means that a frequency spectrum will be transformed into a time domain
 * signal and placed in a provided sample buffer. The length of the time domain
 * signal will be `timeSize()` long. The `set` and
 * `scale` functions allow you the ability to shape the spectrum
 * already stored in the object before taking the inverse transform. You might
 * use these to filter frequencies in a spectrum or modify it in some other way.
 *
 * @author Damien Di Fede
 * @see [The Fast Fourier Transform](http://www.dspguide.com/ch12.0.htm)
 */
class FFT(
    private val timeSize: Int,
    private val sampleRate: Float
) {

    private val log = LoggerFactory.getLogger(FFT::class.java)

    private val bandWidth: Float = 2f / timeSize * (sampleRate / 2f)
    private val currentWindow: RectangularWindow

    private var real: FloatArray = floatArrayOf()
    private var imag: FloatArray = floatArrayOf()
    private var spectrum: FloatArray = floatArrayOf()
    private var averages: FloatArray = floatArrayOf()
    private var whichAverage = 0
    private var octaves = 0
    private var avgPerOctave = 0
    private var reverse: IntArray = intArrayOf()
    private var sinlookup: FloatArray = floatArrayOf()
    private var coslookup: FloatArray = floatArrayOf()

    /**
     * Constructs an FFT that will accept sample buffers that are
     * `timeSize` long and have been recorded with a sample rate of
     * `sampleRate`. `timeSize` *must* be a
     * power of two. This will throw an exception if it is not.
     *
     */
    init {
        noAverages()
        allocateArrays()
        currentWindow = RectangularWindow() // a Rectangular window is analogous to using no window.
        require(timeSize and timeSize - 1 == 0) { "FFT: timeSize must be a power of two." }
        buildReverseTable()
        buildTrigTables()
    }

    /**
     * Sets the object to not compute averages.
     *
     */
    private fun noAverages() {
        averages = floatArrayOf()
        whichAverage = NOAVG
    }

    private fun allocateArrays() {
        spectrum = FloatArray(timeSize / 2 + 1) { 0.0F }
        real = FloatArray(timeSize) { 0.0F }
        imag = FloatArray(timeSize) { 0.0F }
    }

    private fun buildReverseTable() {
        reverse = IntArray(timeSize)

        // set up the bit reversing table
        reverse[0] = 0
        var limit = 1
        var bit = timeSize / 2
        while (limit < timeSize) {
            for (i in 0 until limit) reverse[i + limit] = reverse[i] + bit
            limit = limit shl 1
            bit = bit shr 1
        }
    }

    private fun buildTrigTables() {
        sinlookup = FloatArray(timeSize) { 0.0F }
        coslookup = FloatArray(timeSize) { 0.0F }
        for (i in 0 until timeSize) {
            sinlookup[i] = kotlin.math.sin((-Math.PI.toFloat() / i).toDouble()).toFloat()
            coslookup[i] = kotlin.math.cos((-Math.PI.toFloat() / i).toDouble()).toFloat()
        }
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
        }
        else {
            FloatArray(numAvg) { 0.0F }
        }
        whichAverage = LINAVG
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
        var nyq = sampleRate.toFloat() / 2f
        octaves = 1
        while (nyq > minBandwidth) {
            nyq /= 2
            octaves++
        }
        log.debug("Number of octaves = $octaves")
        avgPerOctave = bandsPerOctave
        averages = FloatArray(octaves * bandsPerOctave) { 0.0F }
        whichAverage = LOGAVG
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
     * @return float: the bandwidth of the request average band, in Hertz.
     * @see .getAverageCenterFrequency
     */
    fun getAverageBandWidth(averageIndex: Int): Float {
        if (whichAverage == LINAVG) {
            // an average represents a certain number of bands in the spectrum
            val avgWidth = spectrum.size / averages.size
            return avgWidth * bandWidth
        }
        else if (whichAverage == LOGAVG) {
            // which "octave" is this index in?
            val octave = averageIndex / avgPerOctave
            return getFreqStep(octave)
        }
        return 0.0F
    }

    private fun getFreqStep(octave: Int): Float {
        val freqStep: Float
        // figure out the low frequency for this octave
        val lowFreq: Float = if (octave == 0) {
            0f
        }
        else {
            sampleRate / 2 / 2.0.pow((octaves - octave).toDouble()).toFloat()
        }
        // and the high frequency for this octave
        val hiFreq: Float = sampleRate / 2 / 2.0.pow((octaves - octave - 1).toDouble()).toFloat()
        // each average band within the octave will be this big
        freqStep = (hiFreq - lowFreq) / avgPerOctave
        return freqStep
    }

    /**
     * Returns the center frequency of the i<sup>th</sup> average band.
     *
     * @param i int: which average band you want the center frequency of.
     * @return float: the center frequency of the i<sup>th</sup> average band.
     */
    fun getAverageCenterFrequency(i: Int): Float {
        if (whichAverage == LINAVG) {
            // an average represents a certain number of bands in the spectrum
            val avgWidth = spectrum.size / averages.size
            // the "center" bin of the average, this is fudgy.
            val centerBinIndex = i * avgWidth + avgWidth / 2
            return indexToFreq(centerBinIndex)
        }
        else if (whichAverage == LOGAVG) {
            // which "octave" is this index in?
            val octave = i / avgPerOctave
            // which band within that octave is this?
            val offset = i % avgPerOctave
            val freqStep: Float
            // figure out the low frequency for this octave
            val lowFreq: Float = if (octave == 0) {
                0f
            }
            else {
                sampleRate / 2 / 2.0.pow((octaves - octave).toDouble()).toFloat()
            }
            // and the high frequency for this octave
            val hiFreq: Float = sampleRate / 2 / 2.0.pow((octaves - octave - 1).toDouble()).toFloat()
            // each average band within the octave will be this big
            freqStep = (hiFreq - lowFreq) / avgPerOctave
            // figure out the low frequency of the band we care about
            val f = lowFreq + offset * freqStep
            // the center of the band will be the low plus half the width
            return f + freqStep / 2
        }
        return 0.0F
    }

    /**
     * Returns the middle frequency of the i<sup>th</sup> band.
     *
     * @param i int: the index of the band you want to middle frequency of
     * @return float: the middle frequency, in Hertz, of the requested band of the spectrum
     */
    private fun indexToFreq(i: Int): Float {
        val bw: Float = bandWidth
        // special case: the width of the first bin is half that of the others.
        //               so the center frequency is a quarter of the way.
        if (i == 0) {
            return bw * 0.25f
        }
        // special case: the width of the last bin is half that of the others.
        if (i == spectrum.size - 1) {
            val lastBinBeginFreq = sampleRate.toFloat() / 2 - bw / 2
            val binHalfWidth = bw * 0.25f
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
     * @param freq float: the frequency in Hz
     * @return float: the amplitude of the frequency in the spectrum
     */
    fun getFreq(freq: Float): Float {
        return getBand(freqToIndex(freq))
    }

    /**
     * Returns the amplitude of the requested frequency band.
     *
     * @param i int: the index of a frequency band
     * @return float: the amplitude of the requested frequency band
     */
    fun getBand(i: Int): Float {
        var q = i
        if (q < 0) {
            q = 0
        }
        if (q > spectrum.size - 1) {
            q = spectrum.size - 1
        }
        return spectrum[q]
    }

    /**
     * Returns the index of the frequency band that contains the requested
     * frequency.
     *
     * @param freq float: the frequency you want the index for (in Hz)
     * @return int: the index of the frequency band that contains freq
     */
    private fun freqToIndex(freq: Float): Int {
        // special case: freq is lower than the bandwidth of spectrum[0]
        if (freq < bandWidth / 2) {
            return 0
        }
        // special case: freq is within the bandwidth of spectrum[spectrum.length - 1]
        if (freq > sampleRate.toFloat() / 2 - bandWidth / 2) {
            return spectrum.size - 1
        }
        // all other cases
        val fraction = freq / sampleRate.toFloat()
        return Math.round(timeSize * fraction)
    }

    /**
     * Sets the amplitude of the requested frequency in the spectrum to
     * `a`.
     *
     * @param freq float: the frequency in Hz
     * @param a    float: the new amplitude
     */
    fun setFreq(freq: Float, a: Float) {
        setBand(freqToIndex(freq), a)
    }

    private fun setBand(i: Int, a: Float) {
        if (a < 0) {
            log.error("Can't set a frequency band to a negative value.")
            return
        }
        if (real[i] == 0f && imag[i] == 0f) {
            real[i] = a
            spectrum[i] = a
        }
        else {
            real[i] /= spectrum[i]
            imag[i] /= spectrum[i]
            spectrum[i] = a
            real[i] *= spectrum[i]
            imag[i] *= spectrum[i]
        }
        if (i != 0 && i != timeSize / 2) {
            real[timeSize - i] = real[i]
            imag[timeSize - i] = -imag[i]
        }
    }

    /**
     * Scales the amplitude of the requested frequency by `a`.
     *
     * @param freq float: the frequency in Hz
     * @param s    float: the scaling factor
     */
    fun scaleFreq(freq: Float, s: Float) {
        scaleBand(freqToIndex(freq), s)
    }

    private fun scaleBand(i: Int, s: Float) {
        if (s < 0) {
            log.error("Can't scale a frequency band by a negative value.")
            return
        }
        real[i] *= s
        imag[i] *= s
        spectrum[i] *= s
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
     * @return float: the value of the requested average band
     */
    fun getAvg(i: Int): Float {
        val ret: Float = if (averages.isNotEmpty()) {
            averages[i]
        }
        else {
            0f
        }
        return ret
    }

    /**
     * Performs a forward transform on `buffer`.
     *
     * @param buffer AudioBuffer: the buffer to analyze
     */
    fun forward(buffer: AudioBuffer) {
        forward(buffer.toArray())
    }

    fun forward(buffer: FloatArray) {
        if (buffer.size != timeSize) {
            log.error("FFT.forward: The length of the passed sample buffer must be equal to timeSize().")
            return
        }
        doWindow(buffer)
        // copy samples to real/imag in bit-reversed order
        bitReverseSamples(buffer, 0)
        // perform the fft
        fft()
        // fill the spectrum buffer with amplitudes
        fillSpectrum()
    }

    // fill the spectrum array with the amps of the data in real and imag
    // used so that this class can handle creating the average array
    // and also do spectrum shaping if necessary
    private fun fillSpectrum() {
        for (i in spectrum.indices) {
            spectrum[i] =
                sqrt((real[i] * real[i] + imag[i] * imag[i]).toDouble())
                    .toFloat()
        }
        if (whichAverage == LINAVG) {
            val avgWidth = spectrum.size / averages.size
            for (i in averages.indices) {
                var avg = 0f
                var j: Int = 0
                while (j < avgWidth) {
                    val offset = j + i * avgWidth
                    avg += if (offset < spectrum.size) {
                        spectrum[offset]
                    }
                    else {
                        break
                    }
                    j++
                }
                avg /= (j + 1).toFloat()
                averages[i] = avg
            }
        }
        else if (whichAverage == LOGAVG) {
            for (i in 0 until octaves) {
                var freqStep: Float
                val lowFreq: Float = if (i == 0) {
                    0f
                }
                else {
                    sampleRate / 2 / 2.0.pow((octaves - i).toDouble()).toFloat()
                }
                val hiFreq: Float = sampleRate / 2 / 2.0.pow((octaves - i - 1).toDouble()).toFloat()
                freqStep = (hiFreq - lowFreq) / avgPerOctave
                var f = lowFreq
                for (j in 0 until avgPerOctave) {
                    val offset = j + i * avgPerOctave
                    averages[offset] = calcAvg(f, f + freqStep)
                    f += freqStep
                }
            }
        }
    }

    /**
     * Calculate the average amplitude of the frequency band bounded by
     * `lowFreq` and `hiFreq`, inclusive.
     *
     * @param lowFreq float: the lower bound of the band, in Hertz
     * @param hiFreq  float: the upper bound of the band, in Hertz
     * @return float: the average of all spectrum values within the bounds
     */
    private fun calcAvg(lowFreq: Float, hiFreq: Float): Float {
        val lowBound = freqToIndex(lowFreq)
        val hiBound = freqToIndex(hiFreq)
        var avg = 0f
        for (i in lowBound..hiBound) {
            avg += spectrum[i]
        }
        avg /= (hiBound - lowBound + 1).toFloat()
        return avg
    }

    private fun doWindow(samples: FloatArray) {
        currentWindow.apply(samples)
    }

    // performs an in-place fft on the data in the real and imag arrays
    // bit reversing is not necessary as the data will already be bit reversed
    private fun fft() {
        var halfSize = 1
        while (halfSize < real.size) {

            // float k = -(float)Math.PI/halfSize;
            // phase shift step
            // float phaseShiftStepR = (float)Math.cos(k);
            // float phaseShiftStepI = (float)Math.sin(k);
            // using lookup table
            val phaseShiftStepR = cos(halfSize)
            val phaseShiftStepI = sin(halfSize)
            // current phase shift
            var currentPhaseShiftR = 1.0f
            var currentPhaseShiftI = 0.0f
            for (fftStep in 0 until halfSize) {
                var i = fftStep
                while (i < real.size) {
                    val off = i + halfSize
                    val tr = currentPhaseShiftR * real[off] - currentPhaseShiftI * imag[off]
                    val ti = currentPhaseShiftR * imag[off] + currentPhaseShiftI * real[off]
                    real[off] = real[i] - tr
                    imag[off] = imag[i] - ti
                    real[i] += tr
                    imag[i] += ti
                    i += 2 * halfSize
                }
                val tmpR = currentPhaseShiftR
                currentPhaseShiftR = tmpR * phaseShiftStepR - currentPhaseShiftI * phaseShiftStepI
                currentPhaseShiftI = tmpR * phaseShiftStepI + currentPhaseShiftI * phaseShiftStepR
            }
            halfSize *= 2
        }
    }

    private fun sin(i: Int): Float {
        return sinlookup[i]
    }

    private fun cos(i: Int): Float {
        return coslookup[i]
    }

    // copies the values in the samples array into the real array
    // in bit reversed order. the imag array is filled with zeros.
    private fun bitReverseSamples(samples: FloatArray, startAt: Int) {
        for (i in 0 until timeSize) {
            real[i] = samples[startAt + reverse[i]]
            imag[i] = 0.0f
        }
    }

    /**
     * Performs a forward transform on `buffer`.
     *
     * @param buffer  AudioBuffer: the buffer to analyze
     * @param startAt int: the index to start at in the buffer. there must be at least timeSize() samples
     * between the starting index and the end of the buffer. If there aren't, an
     * error will be issued and the operation will not be performed.
     */
    fun forward(buffer: AudioBuffer, startAt: Int) {
        forward(buffer.toArray(), startAt)
    }

    // lookup tables
    private fun forward(buffer: FloatArray, startAt: Int) {
        if (buffer.size - startAt < timeSize) {
            log.error(
                "FourierTransform.forward: not enough samples in the buffer between " +
                        startAt + " and " + buffer.size + " to perform a transform."
            )
            return
        }
        currentWindow.apply(buffer, startAt, timeSize)
        bitReverseSamples(buffer, startAt)
        fft()
        fillSpectrum()
    }

    /**
     * Performs a forward transform on the passed buffers.
     *
     * @param buffReal the real part of the time domain signal to transform
     * @param buffImag the imaginary part of the time domain signal to transform
     */
    fun forward(buffReal: FloatArray, buffImag: FloatArray) {
        if (buffReal.size != timeSize || buffImag.size != timeSize) {
            log.error("FFT.forward: The length of the passed buffers must be equal to timeSize().")
            return
        }
        setComplex(buffReal, buffImag)
        bitReverseComplex()
        fft()
        fillSpectrum()
    }

    private fun setComplex(r: FloatArray, i: FloatArray) {
        if (real.size != r.size && imag.size != i.size) {
            log.error("FourierTransform.setComplex: the two arrays must be the same length as their member counterparts.")
        }
        else {
            System.arraycopy(r, 0, real, 0, r.size)
            System.arraycopy(i, 0, imag, 0, i.size)
        }
    }

    // bit reverse real[] and imag[]
    private fun bitReverseComplex() {
        val revReal = FloatArray(real.size) { 0.0F }
        val revImag = FloatArray(imag.size) { 0.0F }
        for (i in real.indices) {
            revReal[i] = real[reverse[i]]
            revImag[i] = imag[reverse[i]]
        }
        real = revReal
        imag = revImag
    }

    fun inverse(buffer: FloatArray) {
        if (buffer.size > real.size) {
            log.error("FFT.inverse: the passed array's length must equal FFT.timeSize().")
            return
        }
        // conjugate
        for (i in 0 until timeSize) {
            imag[i] *= -1f
        }
        bitReverseComplex()
        fft()
        // copy the result in real into buffer, scaling as we do
        for (i in buffer.indices) {
            buffer[i] = real[i] / real.size
        }
    }

    companion object {
        private const val LINAVG = 1
        private const val LOGAVG = 2
        private const val NOAVG = 3
    }
}

