package de.visualdigits.kotlin.minim

import javax.sound.sampled.AudioFormat

/**
 * A class for small buffers of samples in linear, 32-bit doubleing point format.
 * <p>
 * It is supposed to be a replacement of the byte[] stream architecture of
 * JavaSound, especially for chains of AudioInputStreams. Ideally, all involved
 * AudioInputStreams handle reading into a FloatSampleBuffer.
 * <p>
 * Specifications:
 * <ol>
 * <li>Channels are separated, i.e. for stereo there are 2 double arrays with
 * the samples for the left and right channel
 * <li>All data is handled in samples, where one sample means one double value
 * in each channel
 * <li>All samples are normalized to the interval [-1.0...1.0]
 * </ol>
 * <p>
 * When a cascade of AudioInputStreams use FloatSampleBuffer for processing,
 * they may implement the interface FloatSampleInput. This signals that this
 * stream may provide double buffers for reading. The data is <i>not</i>
 * converted back to bytes, but stays in a single buffer that is passed from
 * stream to stream. For that serves the read(FloatSampleBuffer) method, which
 * is then used as replacement for the byte-based read functions of
 * AudioInputStream.<br>
 * However, backwards compatibility must always be retained, so even when an
 * AudioInputStream implements FloatSampleInput, it must work the same way when
 * any of the byte-based read methods is called.<br>
 * As an example, consider the following set-up:<br>
 * <ul>
 * <li>auAIS is an AudioInputStream (AIS) that reads from an AU file in 8bit
 * pcm at 8000Hz. It does not implement FloatSampleInput.
 * <li>pcmAIS1 is an AIS that reads from auAIS and converts the data to PCM
 * 16bit. This stream implements FloatSampleInput, i.e. it can generate double
 * audio data from the ulaw samples.
 * <li>pcmAIS2 reads from pcmAIS1 and adds a reverb. It operates entirely on
 * doubleing point samples.
 * <li>The method that reads from pcmAIS2 (i.e. AudioSystem.write) does not
 * handle doubleing point samples.
 * </ul>
 * So, what happens when a block of samples is read from pcmAIS2 ?
 * <ol>
 * <li>the read(byte[]) method of pcmAIS2 is called
 * <li>pcmAIS2 always operates on doubleing point samples, so it uses an own
 * instance of FloatSampleBuffer and initializes it with the number of samples
 * requested in the read(byte[]) method.
 * <li>It queries pcmAIS1 for the FloatSampleInput interface. As it implements
 * it, pcmAIS2 calls the read(FloatSampleBuffer) method of pcmAIS1.
 * <li>pcmAIS1 notes that its underlying stream does not support doubles, so it
 * instantiates a byte buffer which can hold the number of samples of the
 * FloatSampleBuffer passed to it. It calls the read(byte[]) method of auAIS.
 * <li>auAIS fills the buffer with the bytes.
 * <li>pcmAIS1 calls the <code>initFromByteArray</code> method of the double
 * buffer to initialize it with the 8 bit data.
 * <li>Then pcmAIS1 processes the data: as the double buffer is normalized, it
 * does nothing with the buffer - and returns control to pcmAIS2. The
 * SampleSizeInBits field of the AudioFormat of pcmAIS1 defines that it should
 * be 16 bits.
 * <li>pcmAIS2 receives the filled buffer from pcmAIS1 and does its processing
 * on the buffer - it adds the reverb.
 * <li>As pcmAIS2's read(byte[]) method had been called, pcmAIS2 calls the
 * <code>convertToByteArray</code> method of the double buffer to fill the byte
 * buffer with the resulting samples.
 * </ol>
 * <p>
 * To summarize, here are some advantages when using a FloatSampleBuffer for
 * streaming:
 * <ul>
 * <li>no conversions from/to bytes need to be done during processing
 * <li>the sample size in bits is irrelevant - normalized range
 * <li>higher quality for processing
 * <li>separated channels (easy process/remove/add channels)
 * <li>potentially less copying of audio data, as processing the double samples
 * is generally done in-place. The same instance of a FloatSampleBuffer may be
 * used from the original data source to the final data sink.
 * </ul>
 * <p>
 * Simple benchmarks showed that the processing requirements for the conversion
 * to and from double is about the same as when converting it to shorts or ints
 * without dithering, and significantly higher with dithering. An own
 * implementation of a random number generator may improve this.
 * <p>
 * &quot;Lazy&quot; deletion of samples and channels:<br>
 * <ul>
 * <li>When the sample count is reduced, the arrays are not resized, but only
 * the member variable <code>sampleCount</code> is reduced. A subsequent
 * increase of the sample count (which will occur frequently), will check that
 * and eventually reuse the existing array.
 * <li>When a channel is deleted, it is not removed from memory but only
 * hidden. Subsequent insertions of a channel will check whether a hidden
 * channel can be reused.
 * </ul>
 * The lazy mechanism can save many array instantiation (and copy-) operations
 * for the sake of performance. All relevant methods exist in a second version
 * which allows explicitely to disable lazy deletion.
 * <p>
 * Use the <code>reset</code> functions to clear the memory and remove hidden
 * samples and channels.
 * <p>
 * Note that the lazy mechanism implies that the arrays returned from
 * <code>getChannel(int)</code> may have a greater size than sampleCount.
 * Consequently, be sure to never rely on the length field of the sample arrays.
 * <p>
 * As an example, consider a chain of converters that all act on the same
 * instance of FloatSampleBuffer. Some converters may decrease the sample count
 * (e.g. sample rate converter) and delete channels (e.g. PCM2PCM converter).
 * So, processing of one block will decrease both. For the next block, all
 * starts from the beginning. With the lazy mechanism, all double arrays are only
 * created once for processing all blocks.<br>
 * Having lazy disabled would require for each chunk that is processed
 * <ol>
 * <li>new instantiation of all channel arrays at the converter chain beginning
 * as they have been either deleted or decreased in size during processing of
 * the previous chunk, and
 * <li>re-instantiation of all channel arrays for the reduction of the sample
 * count.
 * </ol>
 * <p>
 * Dithering:<br>
 * By default, this class uses dithering for reduction of sample width (e.g.
 * original data was 16bit, target data is 8bit). As dithering may be needed in
 * other cases (especially when the double samples are processed using DSP
 * algorithms), or it is preferred to switch it off, dithering can be
 * explicitely switched on or off with the method setDitherMode(int).<br>
 * For a discussion about dithering, see <a
 * href="http://www.iqsoft.com/IQSMagazine/BobsSoapbox/Dithering.htm"> here</a>
 * and <a href="http://www.iqsoft.com/IQSMagazine/BobsSoapbox/Dithering2.htm">
 * here</a>.
 *
 * @author Florian Bomers (java version)
 */
class DoubleSampleBuffer(
    var sampleCount: Int = 0,
    var channelCount: Int = 0,
    var sampleRate: Double = 0.0
) {

    /**
     * Constant for setDitherMode: dithering will be enabled if sample size is
     * decreased
     */
    private val DITHER_MODE_AUTOMATIC = 0

    /**
     * Constant for setDitherMode: dithering will be done
     */
    private val DITHER_MODE_ON = 1

    /**
     * Constant for setDitherMode: dithering will not be done
     */
    private val DITHER_MODE_OFF = 2

    private val DEFAULT_DITHER_BITS = 0.7

    // one double array for each channel
    private val channels: MutableList<DoubleArray> = MutableList(channelCount) { DoubleArray(sampleCount) }

    // cache for performance
    private var lastConvertToByteArrayFormat: AudioFormat? = null
    private var lastConvertToByteArrayFormatCode = 0

    /**
     * Silence the entire audio buffer.
     */
    fun makeSilence() {
        require(!(sampleCount < 0)) { "offset and/or sampleCount out of bounds" }
        // silence all channels
        val localChannelCount = channelCount
        for (ch in 0 until localChannelCount) {
            val samples = getChannel(ch)
            for (i in 0 until sampleCount) {
                samples[i] = 0.0
            }
        }
    }

    /**
     * Get the actual audio data of one channel.<br></br>
     * Modifying this array will modify the audio samples of this
     * FloatSampleBuffer. <br></br>
     * NOTE: the returned array may be larger than sampleCount. So in any case,
     * sampleCount is to be respected.
     *
     * @throws IllegalArgumentException if channel is out of bounds
     */
    fun getChannel(channel: Int): DoubleArray {
        if (channel >= channelCount) {
            throw IllegalArgumentException(
                "FloatSampleBuffer: invalid channel number."
            )
        }
        return channels[channel]
    }

    /**
     * @return the required size of the buffer for calling
     * convertToByteArray(..) is called
     */
    fun getByteArrayBufferSize(format: AudioFormat): Int {
        // make sure this format is supported
        checkFormatSupported(format)
        return format.frameSize * sampleCount
    }

    /**
     * Verify that the specified AudioFormat can be converted to and from. If
     * the format is not supported, an IllegalArgumentException is thrown.
     *
     * @throws IllegalArgumentException if the format is not supported
     */
    fun checkFormatSupported(format: AudioFormat) {
        DoubleSampleTools.getFormatType(format)
    }

    /**
     * Writes this sample buffer's audio data to `buffer` as an
     * interleaved byte array. `buffer` must be large enough to
     * hold all data.
     *
     * @return number of bytes written to `buffer`
     * @throws IllegalArgumentException when buffer is too small or
     * `format` doesn't match
     */
    fun convertToByteArray(buffer: ByteArray, offset: Int, format: AudioFormat): Int {
        val byteCount = format.frameSize * sampleCount
        if (offset + byteCount > buffer.size) {
            throw IllegalArgumentException(
                "FloatSampleBuffer.convertToByteArray: buffer too small."
            )
        }
        if (format !== lastConvertToByteArrayFormat) {
            if (format.sampleRate.toDouble() != sampleRate) {
                throw IllegalArgumentException(
                    "FloatSampleBuffer.convertToByteArray: different samplerates."
                )
            }
            if (format.channels != channelCount) {
                throw IllegalArgumentException(
                    "FloatSampleBuffer.convertToByteArray: different channel count."
                )
            }
            lastConvertToByteArrayFormat = format
            lastConvertToByteArrayFormatCode = DoubleSampleTools.getFormatType(format)
        }
        DoubleSampleTools.double2byte(
            channels, buffer, offset,
            sampleCount, lastConvertToByteArrayFormatCode,
            format.channels, format.frameSize,
            getConvertDitherBits(lastConvertToByteArrayFormatCode)
        )
        return byteCount
    }

    /**
     * @return the ditherBits parameter for the double2byte functions
     */
    protected fun getConvertDitherBits(newFormatType: Int): Double {
        var doDither = false
        when (newFormatType) {
            DITHER_MODE_AUTOMATIC, DITHER_MODE_OFF -> {}
            DITHER_MODE_ON -> doDither = true
        }
        return if (doDither) DEFAULT_DITHER_BITS else 0.0
    }

    /**
     * Initializes audio data from the provided byte array. The double samples
     * are written at `destOffset`. This FloatSampleBuffer must be
     * big enough to accomodate the samples.
     *
     *
     * `srcBuffer` is read from index `srcOffset` to
     * `(srcOffset + (lengthInSamples * format.getFrameSize()))`.
     *
     * @param input        the input buffer in interleaved audio data
     * @param inByteOffset the offset in `input`
     * @param format       input buffer's audio format
     * @param doubleOffset  the offset where to write the double samples
     * @param frameCount   number of samples to write to this sample buffer
     */
    fun setSamplesFromBytes(
        input: ByteArray, inByteOffset: Int,
        format: AudioFormat, doubleOffset: Int, frameCount: Int
    ) {
        if (doubleOffset < 0 || frameCount < 0 || inByteOffset < 0) {
            throw IllegalArgumentException(
                "FloatSampleBuffer.setSamplesFromBytes: negative inByteOffset, doubleOffset, or frameCount"
            )
        }
        if (inByteOffset + frameCount * format.frameSize > input.size) {
            throw IllegalArgumentException(
                "FloatSampleBuffer.setSamplesFromBytes: input buffer too small."
            )
        }
        if (doubleOffset + frameCount > sampleCount) {
            throw IllegalArgumentException(
                "FloatSampleBuffer.setSamplesFromBytes: frameCount too large"
            )
        }
        DoubleSampleTools.byte2double(
            input, inByteOffset, channels, doubleOffset,
            frameCount, format, false
        )
    }
}
