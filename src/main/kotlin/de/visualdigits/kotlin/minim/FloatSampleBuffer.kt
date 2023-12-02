package de.visualdigits.kotlin.minim

import javax.sound.sampled.AudioFormat

/**
 * A class for small buffers of samples in linear, 32-bit floating point format.
 *
 *
 * It is supposed to be a replacement of the byte[] stream architecture of
 * JavaSound, especially for chains of AudioInputStreams. Ideally, all involved
 * AudioInputStreams handle reading into a FloatSampleBuffer.
 *
 *
 * Specifications:
 *
 *  1. Channels are separated, i.e. for stereo there are 2 float arrays with
 * the samples for the left and right channel
 *  1. All data is handled in samples, where one sample means one float value
 * in each channel
 *  1. All samples are normalized to the interval [-1.0...1.0]
 *
 *
 *
 * When a cascade of AudioInputStreams use FloatSampleBuffer for processing,
 * they may implement the interface FloatSampleInput. This signals that this
 * stream may provide float buffers for reading. The data is *not*
 * converted back to bytes, but stays in a single buffer that is passed from
 * stream to stream. For that serves the read(FloatSampleBuffer) method, which
 * is then used as replacement for the byte-based read functions of
 * AudioInputStream.<br></br>
 * However, backwards compatibility must always be retained, so even when an
 * AudioInputStream implements FloatSampleInput, it must work the same way when
 * any of the byte-based read methods is called.<br></br>
 * As an example, consider the following set-up:<br></br>
 *
 *  * auAIS is an AudioInputStream (AIS) that reads from an AU file in 8bit
 * pcm at 8000Hz. It does not implement FloatSampleInput.
 *  * pcmAIS1 is an AIS that reads from auAIS and converts the data to PCM
 * 16bit. This stream implements FloatSampleInput, i.e. it can generate float
 * audio data from the ulaw samples.
 *  * pcmAIS2 reads from pcmAIS1 and adds a reverb. It operates entirely on
 * floating point samples.
 *  * The method that reads from pcmAIS2 (i.e. AudioSystem.write) does not
 * handle floating point samples.
 *
 * So, what happens when a block of samples is read from pcmAIS2 
 *
 *  1. the read(byte[]) method of pcmAIS2 is called
 *  1. pcmAIS2 always operates on floating point samples, so it uses an own
 * instance of FloatSampleBuffer and initializes it with the number of samples
 * requested in the read(byte[]) method.
 *  1. It queries pcmAIS1 for the FloatSampleInput interface. As it implements
 * it, pcmAIS2 calls the read(FloatSampleBuffer) method of pcmAIS1.
 *  1. pcmAIS1 notes that its underlying stream does not support floats, so it
 * instantiates a byte buffer which can hold the number of samples of the
 * FloatSampleBuffer passed to it. It calls the read(byte[]) method of auAIS.
 *  1. auAIS fills the buffer with the bytes.
 *  1. pcmAIS1 calls the `initFromByteArray` method of the float
 * buffer to initialize it with the 8 bit data.
 *  1. Then pcmAIS1 processes the data: as the float buffer is normalized, it
 * does nothing with the buffer - and returns control to pcmAIS2. The
 * SampleSizeInBits field of the AudioFormat of pcmAIS1 defines that it should
 * be 16 bits.
 *  1. pcmAIS2 receives the filled buffer from pcmAIS1 and does its processing
 * on the buffer - it adds the reverb.
 *  1. As pcmAIS2's read(byte[]) method had been called, pcmAIS2 calls the
 * `convertToByteArray` method of the float buffer to fill the byte
 * buffer with the resulting samples.
 *
 *
 *
 * To summarize, here are some advantages when using a FloatSampleBuffer for
 * streaming:
 *
 *  * no conversions from/to bytes need to be done during processing
 *  * the sample size in bits is irrelevant - normalized range
 *  * higher quality for processing
 *  * separated channels (easy process/remove/add channels)
 *  * potentially less copying of audio data, as processing the float samples
 * is generally done in-place. The same instance of a FloatSampleBuffer may be
 * used from the original data source to the final data sink.
 *
 *
 *
 * Simple benchmarks showed that the processing requirements for the conversion
 * to and from float is about the same as when converting it to shorts or ints
 * without dithering, and significantly higher with dithering. An own
 * implementation of a random number generator may improve this.
 *
 *
 * &quot;Lazy&quot; deletion of samples and channels:<br></br>
 *
 *  * When the sample count is reduced, the arrays are not resized, but only
 * the member variable `sampleCount` is reduced. A subsequent
 * increase of the sample count (which will occur frequently), will check that
 * and eventually reuse the existing array.
 *  * When a channel is deleted, it is not removed from memory but only
 * hidden. Subsequent insertions of a channel will check whether a hidden
 * channel can be reused.
 *
 * The lazy mechanism can save many array instantiation (and copy-) operations
 * for the sake of performance. All relevant methods exist in a second version
 * which allows explicitely to disable lazy deletion.
 *
 *
 * Use the `reset` functions to clear the memory and remove hidden
 * samples and channels.
 *
 *
 * Note that the lazy mechanism implies that the arrays returned from
 * `getChannel(int)` may have a greater size than getSampleCount().
 * Consequently, be sure to never rely on the length field of the sample arrays.
 *
 *
 * As an example, consider a chain of converters that all act on the same
 * instance of FloatSampleBuffer. Some converters may decrease the sample count
 * (e.g. sample rate converter) and delete channels (e.g. PCM2PCM converter).
 * So, processing of one block will decrease both. For the next block, all
 * starts from the beginning. With the lazy mechanism, all float arrays are only
 * created once for processing all blocks.<br></br>
 * Having lazy disabled would require for each chunk that is processed
 *
 *  1. new instantiation of all channel arrays at the converter chain beginning
 * as they have been either deleted or decreased in size during processing of
 * the previous chunk, and
 *  1. re-instantiation of all channel arrays for the reduction of the sample
 * count.
 *
 *
 *
 * Dithering:<br></br>
 * By default, this class uses dithering for reduction of sample width (e.g.
 * original data was 16bit, target data is 8bit). As dithering may be needed in
 * other cases (especially when the float samples are processed using DSP
 * algorithms), or it is preferred to switch it off, dithering can be
 * explicitely switched on or off with the method setDitherMode(int).<br></br>
 * For a discussion about dithering, see [ here](http://www.iqsoft.com/IQSMagazine/BobsSoapbox/Dithering.htm)
 * and [
 * here](http://www.iqsoft.com/IQSMagazine/BobsSoapbox/Dithering2.htm).
 *
 * @author Florian Bomers
 */
class FloatSampleBuffer @JvmOverloads constructor(
    var channelCount: Int = 0,
    var sampleCount: Int = 0,
    var sampleRate: Float = 1f
) {
    // one float array for each channel
    private var channels = Array<FloatArray>(2) { floatArrayOf() }

    private var originalFormatType = 0
    private var ditherBits: Float = FloatSampleTools.DEFAULT_DITHER_BITS

    // e.g. the sample rate converter may want to force dithering
    private var ditherMode = DITHER_MODE_AUTOMATIC

    /**
     * Initialize this sample buffer to have the specified channels, sample
     * count, and sample rate. If lazy is true, as much as possible will
     * existing arrays be reused. Otherwise, any hidden channels are freed.
     *
     * @param newChannelCount
     * @param newSampleCount
     * @param newSampleRate
     * @param lazy
     * @throws IllegalArgumentException if newChannelCount or newSampleCount are
     * negative, or newSampleRate is not positive.
     */
    /**
     * Initialize this sample buffer to have the specified channels, sample
     * count, and sample rate. If LAZY_DEFAULT is true, as much as possible will
     * existing arrays be reused. Otherwise, any hidden channels are freed.
     *
     * @param newChannelCount
     * @param newSampleCount
     * @param newSampleRate
     * @throws IllegalArgumentException if newChannelCount or newSampleCount are
     * negative, or newSampleRate is not positive.
     */
    fun init(
        newChannelCount: Int, newSampleCount: Int,
        newSampleRate: Float, lazy: Boolean = LAZY_DEFAULT
    ) {
        if (newChannelCount < 0 || newSampleCount < 0 || newSampleRate <= 0.0f) {
            throw IllegalArgumentException(
                "invalid parameters in initialization of FloatSampleBuffer."
            )
        }
        sampleRate = newSampleRate
        if (sampleCount != newSampleCount
            || channelCount != newChannelCount
        ) {
            createChannels(newChannelCount, newSampleCount, lazy)
        }
    }

    /**
     * Grow the channels array to allow at least channelCount elements. If
     * !lazy, then channels will be resized to be exactly channelCount elements.
     * The new elements will be null.
     *
     * @param newChannelCount
     * @param lazy
     */
    private fun grow(newChannelCount: Int, lazy: Boolean) {
        if (channels.size < newChannelCount || !lazy) {
            val newChannels = Array<FloatArray>(newChannelCount) { floatArrayOf() }
            System.arraycopy(
                channels, 0, newChannels, 0,
                if (channelCount < newChannelCount) channelCount else newChannelCount
            )
            channels = newChannels
        }
    }

    private fun createChannels(
        newChannelCount: Int, newSampleCount: Int,
        lazy: Boolean
    ) {
        // shortcut
        if (lazy && newChannelCount <= channelCount && newSampleCount <= sampleCount) {
            setSampleCountImpl(newSampleCount)
            setChannelCountImpl(newChannelCount)
            return
        }
        setSampleCountImpl(newSampleCount)
        // grow the array, if necessary. Intentionally lazy here!
        grow(newChannelCount, true)
        // lazy delete of all channels. Intentionally lazy !
        setChannelCountImpl(0)
        for (ch in 0 until newChannelCount) {
            insertChannel(ch, false, lazy)
        }
        // if not lazy, remove hidden channels
        grow(newChannelCount, lazy)
    }
    /**
     * Resets this buffer with the audio data specified in the arguments. This
     * FloatSampleBuffer's sample count will be set to
     * `byteCount / format.getFrameSize()`.
     *
     * @param lazy if true, then existing channels will be tried to be re-used
     * to minimize garbage collection.
     * @throws IllegalArgumentException
     */
    /**
     * Resets this buffer with the audio data specified in the arguments. This
     * FloatSampleBuffer's sample count will be set to
     * `byteCount / format.getFrameSize()`. If LAZY_DEFAULT is
     * true, it will use lazy deletion.
     *
     * @throws IllegalArgumentException
     */
    @JvmOverloads
    fun initFromByteArray(
        buffer: ByteArray, offset: Int, byteCount: Int,
        format: AudioFormat, lazy: Boolean = LAZY_DEFAULT
    ) {
        if (offset + byteCount > buffer.size) {
            throw IllegalArgumentException(
                "FloatSampleBuffer.initFromByteArray: buffer too small."
            )
        }
        val thisSampleCount = byteCount / format.frameSize
        init(
            format.channels, thisSampleCount, format.sampleRate,
            lazy
        )

        // save format for automatic dithering mode
        originalFormatType = FloatSampleTools.getFormatType(format)
        FloatSampleTools.byte2float(
            buffer, offset, channels, 0, sampleCount,
            format
        )
    }

    /**
     * Resets this sample buffer with the data in `source`.
     */
    fun initFromFloatSampleBuffer(source: FloatSampleBuffer) {
        init(
            source.channelCount, source.sampleCount,
            source.sampleRate
        )
        for (ch in 0 until channelCount) {
            System.arraycopy(
                source.getChannel(ch), 0, getChannel(ch), 0,
                sampleCount
            )
        }
    }

    /**
     * Write the contents of the byte array to this buffer, overwriting existing
     * data. If the byte array has fewer channels than this float buffer, only
     * the first channels are written. Vice versa, if the byte buffer has more
     * channels than this float buffer, only the first channels of the byte
     * buffer are written to this buffer.
     *
     *
     * The format and the number of samples of this float buffer are not
     * changed, so if the byte array has more samples than fit into this float
     * buffer, it is not expanded.
     *
     * @param buffer          the byte buffer to write to this float buffer
     * @param srcByteOffset   the offset in bytes in buffer where to start reading
     * @param format          the audio format of the bytes in buffer
     * @param dstSampleOffset the offset in samples where to start writing the
     * converted float data into this float buffer
     * @param aSampleCount    the number of samples to write
     * @return the number of samples actually written
     */
    fun writeByteBuffer(
        buffer: ByteArray, srcByteOffset: Int,
        format: AudioFormat, dstSampleOffset: Int, aSampleCount: Int
    ): Int {
        var srcByteOffset = srcByteOffset
        var aSampleCount = aSampleCount
        if (dstSampleOffset + aSampleCount > sampleCount) {
            aSampleCount = sampleCount - dstSampleOffset
        }
        var lChannels = format.channels
        if (lChannels > channelCount) {
            lChannels = channelCount
        }
        if (lChannels > format.channels) {
            lChannels = format.channels
        }
        for (channel in 0 until lChannels) {
            val data = getChannel(channel)
            FloatSampleTools.byte2floatGeneric(
                buffer, srcByteOffset,
                format.frameSize, data, dstSampleOffset, aSampleCount,
                format
            )
            srcByteOffset += format.frameSize / format.channels
        }
        return aSampleCount
    }

    /**
     * Deletes all channels, frees memory... This also removes hidden channels
     * by lazy remove.
     */
    fun reset() {
        init(0, 0, 1f, false)
    }

    /**
     * Destroys any existing data and creates new channels. It also destroys
     * lazy removed channels and samples. Channels will not be silenced, though.
     */
    fun reset(newChannels: Int, newSampleCount: Int, newSampleRate: Float) {
        init(newChannels, newSampleCount, newSampleRate, false)
    }
    // //////////////////////// conversion back to bytes ///////////////////
    /**
     * @return the required size of the buffer for calling
     * convertToByteArray(..) is called
     */
    fun getByteArrayBufferSize(format: AudioFormat): Int {
        return getByteArrayBufferSize(format, sampleCount)
    }

    /**
     * @param lenInSamples how many samples to be considered
     * @return the required size of the buffer for the given number of samples
     * for calling convertToByteArray(..)
     */
    fun getByteArrayBufferSize(format: AudioFormat, lenInSamples: Int): Int {
        // make sure this format is supported
        checkFormatSupported(format)
        return format.frameSize * lenInSamples
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
        return convertToByteArray(0, sampleCount, buffer, offset, format)
    }

    // cache for performance
    private var lastConvertToByteArrayFormat: AudioFormat? = null
    private var lastConvertToByteArrayFormatCode = 0
    /**
     * Create an empty FloatSampleBuffer with the specified number of channels,
     * samples, and the specified sample rate.
     */
    // ////////////////////////////// initialization //////////////////////
    /**
     * Create an instance with initially no channels.
     */
    init {
        init(channelCount, sampleCount, sampleRate, LAZY_DEFAULT)
    }

    /**
     * Writes this sample buffer's audio data to `buffer` as an
     * interleaved byte array. `buffer` must be large enough to
     * hold all data.
     *
     * @param readOffset   the sample offset from where samples are read from this
     * FloatSampleBuffer
     * @param lenInSamples how many samples are converted
     * @param buffer       the byte buffer written to
     * @param writeOffset  the byte offset in buffer
     * @return number of bytes written to `buffer`
     * @throws IllegalArgumentException when buffer is too small or
     * `format` doesn't match
     */
    fun convertToByteArray(
        readOffset: Int, lenInSamples: Int,
        buffer: ByteArray, writeOffset: Int, format: AudioFormat
    ): Int {
        val byteCount = format.frameSize * lenInSamples
        if (writeOffset + byteCount > buffer.size) {
            throw IllegalArgumentException(
                "FloatSampleBuffer.convertToByteArray: buffer too small."
            )
        }
        if (format !== lastConvertToByteArrayFormat) {
            if (format.sampleRate != sampleRate) {
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
            lastConvertToByteArrayFormatCode = FloatSampleTools.getFormatType(format)
        }
        FloatSampleTools.float2byte(
            channels, readOffset, buffer, writeOffset,
            lenInSamples, lastConvertToByteArrayFormatCode,
            format.channels, format.frameSize,
            getConvertDitherBits(lastConvertToByteArrayFormatCode)
        )
        return byteCount
    }

    /**
     * Creates a new byte[] buffer, fills it with the audio data, and returns
     * it.
     *
     * @throws IllegalArgumentException when sample rate or channels do not
     * match
     * @see .convertToByteArray
     */
    fun convertToByteArray(format: AudioFormat): ByteArray {
        // throws exception when sampleRate doesn't match
        // creates a new byte[] buffer and returns it
        val res = ByteArray(getByteArrayBufferSize(format))
        convertToByteArray(res, 0, format)
        return res
    }
    // ////////////////////////////// actions /////////////////////////////////
    /**
     * Resizes this buffer.
     *
     *
     * If `keepOldSamples` is true, as much as possible samples are
     * retained. If the buffer is enlarged, silence is added at the end. If
     * `keepOldSamples` is false, existing samples may get
     * discarded, the buffer may then contain random samples.
     */
    fun changeSampleCount(newSampleCount: Int, keepOldSamples: Boolean) {
        val oldSampleCount = sampleCount

        // shortcut: if we just make this buffer smaller, just set new
        // sampleCount
        if (oldSampleCount >= newSampleCount) {
            setSampleCountImpl(newSampleCount)
            return
        }
        // shortcut for one or 2 channels
        if (channelCount == 1 || channelCount == 2) {
            var ch = getChannel(0)
            if (ch!!.size < newSampleCount) {
                val newCh = FloatArray(newSampleCount)
                if (keepOldSamples && oldSampleCount > 0) {
                    // copy old samples
                    System.arraycopy(ch, 0, newCh, 0, oldSampleCount)
                }
                channels[0] = newCh
            }
            else if (keepOldSamples) {
                // silence out excess samples (according to the specification)
                for (i in oldSampleCount until newSampleCount) {
                    ch[i] = 0.0f
                }
            }
            if (channelCount == 2) {
                ch = getChannel(1)
                if (ch!!.size < newSampleCount) {
                    val newCh = FloatArray(newSampleCount)
                    if (keepOldSamples && oldSampleCount > 0) {
                        // copy old samples
                        System.arraycopy(ch, 0, newCh, 0, oldSampleCount)
                    }
                    channels[1] = newCh
                }
                else if (keepOldSamples) {
                    // silence out excess samples (according to the
                    // specification)
                    for (i in oldSampleCount until newSampleCount) {
                        ch[i] = 0.0f
                    }
                }
            }
            setSampleCountImpl(newSampleCount)
            return
        }
        var oldChannels: Array<Any?> = arrayOf()
        if (keepOldSamples) {
            oldChannels = getAllChannels()
        }
        init(channelCount, newSampleCount, sampleRate)
        if (keepOldSamples) {
            // copy old channels and eventually silence out new samples
            val copyCount = if (newSampleCount < oldSampleCount) newSampleCount else oldSampleCount
            for (ch in 0 until channelCount) {
                val oldSamples = oldChannels!![ch] as FloatArray
                val newSamples = channels[ch] as FloatArray
                if (oldSamples != newSamples) {
                    // if this sample array was not object of lazy delete
                    System.arraycopy(oldSamples, 0, newSamples, 0, copyCount)
                }
                if (oldSampleCount < newSampleCount) {
                    // silence out new samples
                    for (i in oldSampleCount until newSampleCount) {
                        newSamples!![i] = 0.0f
                    }
                }
            }
        }
    }

    /**
     * Silence the specified channel
     */
    fun makeSilence(
        channel: Int? = null,
        offset: Int = 0,
        count: Int = sampleCount
    ) {
        if (offset < 0 || count + offset > sampleCount || count < 0) {
            throw IllegalArgumentException(
                "offset and/or sampleCount out of bounds"
            )
        }
        if (channel == null) {
            for (ch in 0 until channelCount) {
                makeSilence(getChannel(ch), offset, count)
            }
        } else {
            makeSilence(getChannel(channel), offset, count)
        }
    }

    private fun makeSilence(
        samples: FloatArray,
        offset: Int,
        count: Int
    ) {
        var count = count
        count += offset
        for (i in offset until count) {
            samples!![i] = 0.0f
        }
    }
    /**
     * Fade the volume level of this buffer from the given start volume to the end volume.
     * The fade will start at the offset, and will have reached endVol after count samples.
     * E.g. to implement a fade in, use startVol=0 and endVol=1.
     *
     * @param startVol the start volume as a linear factor [0..1]
     * @param endVol   the end volume as a linear factor [0..1]
     * @param offset   the offset in this buffer where to start the fade (in samples)
     * @param count    the number of samples to fade
     */
    /**
     * Fade the volume level of this buffer from the given start volume to the end volume.
     * E.g. to implement a fade in, use startVol=0 and endVol=1.
     *
     * @param startVol the start volume as a linear factor [0..1]
     * @param endVol   the end volume as a linear factor [0..1]
     */
    @JvmOverloads
    fun linearFade(startVol: Float, endVol: Float, offset: Int = 0, count: Int = sampleCount) {
        for (channel in 0 until channelCount) {
            linearFade(channel, startVol, endVol, offset, count)
        }
    }

    /**
     * Fade the volume level of the specified channel from the given start volume to
     * the end volume.
     * The fade will start at the offset, and will have reached endVol after count
     * samples.
     * E.g. to implement a fade in, use startVol=0 and endVol=1.
     *
     * @param channel  the channel to do the fade
     * @param startVol the start volume as a linear factor [0..1]
     * @param endVol   the end volume as a linear factor [0..1]
     * @param offset   the offset in this buffer where to start the fade (in samples)
     * @param count    the number of samples to fade
     */
    fun linearFade(channel: Int, startVol: Float, endVol: Float, offset: Int, count: Int) {
        if (count <= 0) {
            return
        }
        val end = (count + offset).toFloat()
        val inc = (endVol - startVol) / count
        val samples = getChannel(channel)
        var curr = startVol
        var i = offset
        while (i < end) {
            samples!![i] *= curr
            curr += inc
            i++
        }
    }

    /**
     * Add a channel to this buffer, e.g. adding a channel to a mono buffer will make it a stereo buffer.
     *
     * @param silent if true, the channel is explicitly silenced. Otherwise the new channel may contain random data.
     */
    fun addChannel(silent: Boolean) {
        // creates new, silent channel
        insertChannel(channelCount, silent)
    }
    /**
     * Inserts a channel at position `index`.
     *
     *
     * If `silent` is true, the new channel will be silent.
     * Otherwise it will contain random data.
     *
     *
     * If `lazy` is true, hidden channels which have at least
     * getSampleCount() elements will be examined for reusage as inserted
     * channel.<br></br>
     * If `lazy` is false, still hidden channels are reused, but it
     * is assured that the inserted channel has exactly getSampleCount()
     * elements, thus not wasting memory.
     */
    /**
     * Insert a (silent) channel at position `index`. If
     * LAZY_DEFAULT is true, this is done lazily.
     */
    @JvmOverloads
    fun insertChannel(index: Int, silent: Boolean, lazy: Boolean = LAZY_DEFAULT) {
        // first grow the array of channels, if necessary. Intentionally lazy
        grow(channelCount + 1, true)
        val physSize = channels.size
        val virtSize = channelCount
        var newChannel: FloatArray? = null
        if (physSize > virtSize) {
            // there are hidden channels. Try to use one.
            for (ch in virtSize until physSize) {
                val thisChannel = channels[ch] as FloatArray
                if (thisChannel != null
                    && (lazy && thisChannel.size >= sampleCount || !lazy && thisChannel.size == sampleCount)
                ) {
                    // we found a matching channel. Use it !
                    newChannel = thisChannel
                    channels[ch] = floatArrayOf()
                    break
                }
            }
        }
        if (newChannel == null) {
            newChannel = FloatArray(sampleCount)
        }
        // move channels after index
        for (i in index until virtSize) {
            channels[i + 1] = channels[i]
        }
        channels[index] = newChannel
        setChannelCountImpl(channelCount + 1)
        if (silent) {
            makeSilence(index)
        }
        // if not lazy, remove old channels
        grow(channelCount, lazy)
    }
    /**
     * Removes a channel. If lazy is true, the channel is not physically
     * removed, but only hidden. These hidden channels are reused by subsequent
     * calls to addChannel or insertChannel.
     */
    /**
     * performs a lazy remove of the channel
     */
    @JvmOverloads
    fun removeChannel(channel: Int, lazy: Boolean = LAZY_DEFAULT) {
        val toBeDeleted = channels[channel] as FloatArray
        // move all channels after it
        for (i in channel until channelCount - 1) {
            channels[i] = channels[i + 1]
        }
        if (!lazy) {
            grow(channelCount - 1, true)
        }
        else {
            // if not already, insert this channel at the end
            channels[channelCount - 1] = toBeDeleted
        }
        setChannelCountImpl(channelCount - 1)
    }

    /**
     * Copy sourceChannel's audio data to targetChannel, identified by their
     * indices in the channel list. Both source and target channel have to
     * exist. targetChannel will be overwritten
     */
    fun copyChannel(sourceChannel: Int, targetChannel: Int) {
        val source = getChannel(sourceChannel)
        val target = getChannel(targetChannel)
        System.arraycopy(source, 0, target, 0, sampleCount)
    }

    /**
     * Copy sampleCount samples from sourceChannel at position srcOffset to
     * targetChannel at position targetOffset. sourceChannel and targetChannel
     * are indices in the channel list. Both source and target channel have to
     * exist. targetChannel will be overwritten
     */
    fun copyChannel(
        sourceChannel: Int, sourceOffset: Int,
        targetChannel: Int, targetOffset: Int, aSampleCount: Int
    ) {
        val source = getChannel(sourceChannel)
        val target = getChannel(targetChannel)
        System.arraycopy(
            source, sourceOffset, target, targetOffset,
            aSampleCount
        )
    }

    /**
     * Copies data inside all channel. When the 2 regions overlap, the behavior
     * is not specified.
     */
    fun copy(sourceIndex: Int, destIndex: Int, length: Int) {
        val count = channelCount
        for (i in 0 until count) {
            copy(i, sourceIndex, destIndex, length)
        }
    }

    /**
     * Copies data inside a channel. When the 2 regions overlap, the behavior is
     * not specified.
     */
    fun copy(channel: Int, sourceIndex: Int, destIndex: Int, length: Int) {
        val data = getChannel(channel)
        val bufferCount = sampleCount
        if (sourceIndex + length > bufferCount || destIndex + length > bufferCount || sourceIndex < 0 || destIndex < 0 || length < 0) {
            throw IndexOutOfBoundsException("parameters exceed buffer size")
        }
        System.arraycopy(data, sourceIndex, data, destIndex, length)
    }

    /**
     * Mix up of 1 channel to n channels.<br></br>
     * It copies the first channel to all newly created channels.
     *
     * @param targetChannelCount the number of channels that this sample buffer
     * will have after expanding. NOT the number of channels to add !
     * @throws IllegalArgumentException if this buffer does not have one
     * channel before calling this method.
     */
    fun expandChannel(targetChannelCount: Int) {
        // even more sanity...
        if (channelCount != 1) {
            throw IllegalArgumentException(
                "FloatSampleBuffer: can only expand channels for mono signals."
            )
        }
        for (ch in 1 until targetChannelCount) {
            addChannel(false)
            copyChannel(0, ch)
        }
    }

    /**
     * Mix down of n channels to one channel.<br></br>
     * It uses a simple mixdown: all other channels are added to first channel.<br></br>
     * The volume is NOT lowered ! Be aware, this might cause clipping when
     * converting back to integer samples.
     */
    fun mixDownChannels() {
        val firstChannel = getChannel(0)
        val localSampleCount = sampleCount
        for (ch in channelCount - 1 downTo 1) {
            val thisChannel = getChannel(ch)
            for (i in 0 until localSampleCount) {
                firstChannel!![i] += thisChannel!![i]
            }
            removeChannel(ch)
        }
    }

    /**
     * Mixes `source` to this buffer by adding all samples. At
     * most, `source`'s number of samples, number of channels are
     * mixed. None of the sample count, channel count or sample rate of either
     * buffer are changed. In particular, the caller needs to assure that the
     * sample rate of the buffers match.
     *
     * @param source the buffer to be mixed to this buffer
     */
    fun mix(source: FloatSampleBuffer) {
        var count = sampleCount
        if (count > source.sampleCount) {
            count = source.sampleCount
        }
        var localChannelCount = channelCount
        if (localChannelCount > source.channelCount) {
            localChannelCount = source.channelCount
        }
        for (ch in 0 until localChannelCount) {
            val thisChannel = getChannel(ch)
            val otherChannel = source.getChannel(ch)
            for (i in 0 until count) {
                thisChannel!![i] += otherChannel!![i]
            }
        }
    }

    /**
     * Mixes `source` samples to this buffer by adding the sample values.
     * None of the sample count, channel count or sample rate of either
     * buffer are changed. In particular, the caller needs to assure that the
     * sample rate of the buffers match.
     *
     *
     * This method is not error tolerant, in particular, runtime exceptions
     * will be thrown if the channel counts do not match, or if the
     * offsets and count exceed the buffer's capacity.
     *
     * @param source       the source buffer from where to take samples and mix to this one
     * @param sourceOffset offset in source where to start reading samples
     * @param thisOffset   offset in this buffer from where to start mixing samples
     * @param count        number of samples to mix
     */
    fun mix(source: FloatSampleBuffer, sourceOffset: Int, thisOffset: Int, count: Int) {
        val localChannelCount = channelCount
        for (ch in 0 until localChannelCount) {
            val thisChannel = getChannel(ch)
            val otherChannel = source.getChannel(ch)
            for (i in 0 until count) {
                thisChannel!![i + thisOffset] += otherChannel!![i + sourceOffset]
            }
        }
    }

    /**
     * Copies the contents of this buffer to the destination buffer at the
     * destOffset. At most, `dest`'s number of samples, number of
     * channels are copied. None of the sample count, channel count or sample
     * rate of either buffer are changed. In particular, the caller needs to
     * assure that the sample rate of the buffers match.
     *
     * @param dest       the buffer to write to
     * @param destOffset the position in `dest` where to start
     * writing the samples of this buffer
     * @param count      the number of samples to be copied
     * @return the number of samples copied
     */
    fun copyTo(dest: FloatSampleBuffer, destOffset: Int, count: Int): Int {
        return copyTo(0, dest, destOffset, count)
    }

    /**
     * Copies the specified part of this buffer to the destination buffer.
     * At most, `dest`'s number of samples, number of
     * channels are copied. None of the sample count, channel count or sample
     * rate of either buffer are changed. In particular, the caller needs to
     * assure that the sample rate of the buffers match.
     *
     * @param srcOffset  the start position in this buffer, where to start reading samples
     * @param dest       the buffer to write to
     * @param destOffset the position in `dest` where to start
     * writing the samples
     * @param count      the number of samples to be copied
     * @return the number of samples copied
     */
    fun copyTo(srcOffset: Int, dest: FloatSampleBuffer, destOffset: Int, count: Int): Int {
        var count = count
        if (srcOffset + count > sampleCount) {
            count = sampleCount - srcOffset
        }
        if (count + destOffset > dest.sampleCount) {
            count = dest.sampleCount - destOffset
        }
        var localChannelCount = channelCount
        if (localChannelCount > dest.channelCount) {
            localChannelCount = dest.channelCount
        }
        for (ch in 0 until localChannelCount) {
            System.arraycopy(
                getChannel(ch), srcOffset, dest.getChannel(ch),
                destOffset, count
            )
        }
        return count
    }

    /**
     * Initializes audio data from the provided byte array. The float samples
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
     * @param floatOffset  the offset where to write the float samples
     * @param frameCount   number of samples to write to this sample buffer
     */
    fun setSamplesFromBytes(
        input: ByteArray, inByteOffset: Int,
        format: AudioFormat, floatOffset: Int, frameCount: Int
    ) {
        if (floatOffset < 0 || frameCount < 0 || inByteOffset < 0) {
            throw IllegalArgumentException(
                "FloatSampleBuffer.setSamplesFromBytes: negative inByteOffset, floatOffset, or frameCount"
            )
        }
        if (inByteOffset + frameCount * format.frameSize > input.size) {
            throw IllegalArgumentException(
                "FloatSampleBuffer.setSamplesFromBytes: input buffer too small."
            )
        }
        if (floatOffset + frameCount > sampleCount) {
            throw IllegalArgumentException(
                "FloatSampleBuffer.setSamplesFromBytes: frameCount too large"
            )
        }
        FloatSampleTools.byte2float(
            input, inByteOffset, channels, floatOffset,
            frameCount, format, false
        )
    }

    /**
     * internal setter for channel count, just change the variable. From
     * outside, use addChannel, insertChannel, removeChannel
     */
    protected fun setChannelCountImpl(newChannelCount: Int) {
        if (channelCount != newChannelCount) {
            channelCount = newChannelCount
            // remove cache
            lastConvertToByteArrayFormat = null
        }
    }

    /**
     * internal setter for sample count, just change the variable. From outside,
     * use changeSampleCount
     */
    protected fun setSampleCountImpl(newSampleCount: Int) {
        if (sampleCount != newSampleCount) {
            sampleCount = newSampleCount
        }
    }

    /**
     * Alias for changeSampleCount
     *
     * @param newSampleCount the new number of samples for this buffer
     * @param keepOldSamples if true, the new buffer will keep the current
     * samples in the arrays
     * @see .changeSampleCount
     */
    fun setSampleCount(newSampleCount: Int, keepOldSamples: Boolean) {
        changeSampleCount(newSampleCount, keepOldSamples)
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
    fun getChannel(channel: Int): FloatArray {
        if (channel >= channelCount) {
            throw IllegalArgumentException(
                "FloatSampleBuffer: invalid channel number."
            )
        }
        return channels[channel] as FloatArray
    }

    /**
     * Low-level method to directly set the array for the given channel.
     * Normally, you do not need this method, as you can conveniently
     * resize the array with `changeSampleCount()`. This method
     * may be useful for advanced optimization techniques.
     *
     * @param channel the channel to replace
     * @param data    the audio sample array
     * @return the audio data array that was replaced
     * @throws IllegalArgumentException if channel is out of bounds or data is null
     * @see .changeSampleCount
     */
    fun setRawChannel(channel: Int, data: FloatArray): FloatArray {
        if (data == null) {
            throw IllegalArgumentException(
                "cannot set a channel to a null array"
            )
        }
        val ret = getChannel(channel)
        channels[channel] = data
        return ret
    }

    /**
     * Get an array of all channels.
     *
     * @return all channels as array
     */
    fun  getAllChannels(): Array<Any?> {
            val res = Array<Any?>(channelCount) { null }
            for (ch in 0 until channelCount) {
                res[ch] = getChannel(ch)
            }
            return res
        }

    /**
     * Set the number of bits for dithering. Typically, a value between 0.2 and
     * 0.9 gives best results.
     *
     *
     * Note: this value is only used, when dithering is actually performed.
     */
    fun setDitherBits(ditherBits: Float) {
        if (ditherBits <= 0) {
            throw IllegalArgumentException(
                "DitherBits must be greater than 0"
            )
        }
        this.ditherBits = ditherBits
    }

    fun getDitherBits(): Float {
        return ditherBits
    }

    /**
     * Sets the mode for dithering. This can be one of:
     *
     *  * DITHER_MODE_AUTOMATIC: it is decided automatically, whether
     * dithering is necessary - in general when sample size is decreased.
     *  * DITHER_MODE_ON: dithering will be forced
     *  * DITHER_MODE_OFF: dithering will not be done.
     *
     */
    fun setDitherMode(mode: Int) {
        if (mode != DITHER_MODE_AUTOMATIC && mode != DITHER_MODE_ON && mode != DITHER_MODE_OFF) {
            throw IllegalArgumentException("Illegal DitherMode")
        }
        ditherMode = mode
    }

    fun getDitherMode(): Int {
        return ditherMode
    }

    /**
     * @return the ditherBits parameter for the float2byte functions
     */
    protected fun getConvertDitherBits(newFormatType: Int): Float {
        // let's see whether dithering is necessary
        var doDither = false
        when (ditherMode) {
            DITHER_MODE_AUTOMATIC -> doDither =
                originalFormatType and FloatSampleTools.F_SAMPLE_WIDTH_MASK > newFormatType and FloatSampleTools.F_SAMPLE_WIDTH_MASK

            DITHER_MODE_ON -> doDither = true
            DITHER_MODE_OFF -> doDither = false
        }
        return if (doDither) ditherBits else 0.0f
    }

    companion object {
        /**
         * Whether the functions without lazy parameter are lazy or not.
         */
        private const val LAZY_DEFAULT = true

        /**
         * Constant for setDitherMode: dithering will be enabled if sample size is
         * decreased
         */
        const val DITHER_MODE_AUTOMATIC = 0

        /**
         * Constant for setDitherMode: dithering will be done
         */
        const val DITHER_MODE_ON = 1

        /**
         * Constant for setDitherMode: dithering will not be done
         */
        const val DITHER_MODE_OFF = 2

        /**
         * Verify that the specified AudioFormat can be converted to and from. If
         * the format is not supported, an IllegalArgumentException is thrown.
         *
         * @throws IllegalArgumentException if the format is not supported
         */
        fun checkFormatSupported(format: AudioFormat) {
            FloatSampleTools.getFormatType(format)
        }
    }
}

