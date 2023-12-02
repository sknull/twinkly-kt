package de.visualdigits.kotlin.minim

import java.util.Random
import javax.sound.sampled.AudioFormat

/**
 * Utility functions for handling data in normalized float arrays. Each sample
 * is linear in the range of [-1.0f, +1.0f].
 *
 *
 * Currently, the following bit sizes are supported:
 *
 *  * 8-bit
 *  * 16-bit
 *  * packed 24-bit (stored in 3 bytes)
 *  * unpacked 24-bit (stored in 4 bytes)
 *  * 32-bit
 *
 * 8-bit data can be unsigned or signed. All other data is only supported in
 * signed encoding.
 *
 * @author Florian Bomers
 * @see FloatSampleBuffer
 */
object FloatSampleTools {
    /**
     * default number of bits to be dithered: 0.7f
     */
    val DEFAULT_DITHER_BITS = 0.7f
    private var random: Random? = null

    // sample width (must be in order !)
    val F_8 = 1
    val F_16 = 2
    val F_24_3 = 3
    val F_24_4 = 4
    val F_32 = 5
    val F_SAMPLE_WIDTH_MASK = F_8 or F_16 or F_24_3 or F_24_4 or F_32

    // format bit-flags
    val F_SIGNED = 8
    val F_BIGENDIAN = 16

    // supported formats
    val CT_8S = F_8 or F_SIGNED
    val CT_8U = F_8
    val CT_16SB = F_16 or F_SIGNED or F_BIGENDIAN
    val CT_16SL = F_16 or F_SIGNED
    val CT_24_3SB = F_24_3 or F_SIGNED or F_BIGENDIAN
    val CT_24_3SL = F_24_3 or F_SIGNED
    val CT_24_4SB = F_24_4 or F_SIGNED or F_BIGENDIAN
    val CT_24_4SL = F_24_4 or F_SIGNED
    val CT_32SB = F_32 or F_SIGNED or F_BIGENDIAN
    val CT_32SL = F_32 or F_SIGNED
    // /////////////// FORMAT / FORMAT TYPE /////////////////////////// //
    /**
     * only allow "packed" samples -- currently no support for 18, 20 bits --
     * except 24 bits stored in 4 bytes.
     *
     * @throws IllegalArgumentException
     */
    fun checkSupportedSampleSize(ssib: Int, channels: Int, frameSize: Int) {
        if (ssib == 24 && frameSize == 4 * channels) {
            // 24 bits stored in 4 bytes is OK (24_4)
            return
        }
        if (ssib * channels != frameSize * 8) {
            throw IllegalArgumentException(
                ("unsupported sample size: "
                        + ssib + " bits stored in " + (frameSize / channels)
                        + " bytes.")
            )
        }
    }

    /**
     * Get the formatType code from the given format.
     *
     * @throws IllegalArgumentException
     */
    fun getFormatType(format: AudioFormat): Int {
        val signed = (format.encoding ==
                AudioFormat.Encoding.PCM_SIGNED)
        if ((!signed
                    && format.encoding != AudioFormat.Encoding.PCM_UNSIGNED)
        ) {
            throw IllegalArgumentException(
                "unsupported encoding: only PCM encoding supported."
            )
        }
        if (!signed && format.sampleSizeInBits != 8) {
            throw IllegalArgumentException(
                "unsupported encoding: only 8-bit can be unsigned"
            )
        }
        checkSupportedSampleSize(
            format.sampleSizeInBits,
            format.channels, format.frameSize
        )
        return getFormatType(
            format.sampleSizeInBits,
            format.frameSize / format.channels, signed,
            format.isBigEndian
        )
    }

    /**
     * @throws IllegalArgumentException
     */
    fun getFormatType(
        ssib: Int, bytesPerSample: Int, signed: Boolean,
        bigEndian: Boolean
    ): Int {
        var res = 0
        if (ssib == 24 || (bytesPerSample == ssib / 8)) {
            if (ssib == 8) {
                res = F_8
            }
            else if (ssib == 16) {
                res = F_16
            }
            else if (ssib == 24) {
                if (bytesPerSample == 3) {
                    res = F_24_3
                }
                else if (bytesPerSample == 4) {
                    res = F_24_4
                }
            }
            else if (ssib == 32) {
                res = F_32
            }
        }
        if (res == 0) {
            throw IllegalArgumentException(
                ("ConversionTool: unsupported sample size of " + ssib
                        + " bits per sample in " + bytesPerSample
                        + " bytes.")
            )
        }
        if (!signed && bytesPerSample > 1) {
            throw IllegalArgumentException(
                (
                        "ConversionTool: unsigned samples larger than "
                                + "8 bit are not supported")
            )
        }
        if (signed) {
            res = res or F_SIGNED
        }
        if (bigEndian && (ssib != 8)) {
            res = res or F_BIGENDIAN
        }
        return res
    }

    fun getSampleSize(formatType: Int): Int {
        when (formatType and F_SAMPLE_WIDTH_MASK) {
            F_8 -> return 1
            F_16 -> return 2
            F_24_3 -> return 3
            F_24_4 -> return 4
            F_32 -> return 4
        }
        return 0
    }

    /**
     * Return a string representation of this format
     */
    fun formatType2Str(formatType: Int): String {
        var res = "$formatType: "
        when (formatType and F_SAMPLE_WIDTH_MASK) {
            F_8 -> res += "8bit"
            F_16 -> res += "16bit"
            F_24_3 -> res += "24_3bit"
            F_24_4 -> res += "24_4bit"
            F_32 -> res += "32bit"
        }
        res += if (((formatType and F_SIGNED) == F_SIGNED)) " signed" else " unsigned"
        if ((formatType and F_SAMPLE_WIDTH_MASK) != F_8) {
            res += if (((formatType and F_BIGENDIAN) == F_BIGENDIAN)) " big endian" else " little endian"
        }
        return res
    }

    // /////////////////// BYTE 2 FLOAT /////////////////////////////////// //
    private val twoPower7 = 128.0f
    private val twoPower15 = 32768.0f
    private val twoPower23 = 8388608.0f
    private val twoPower31 = 2147483648.0f
    private val invTwoPower7 = 1 / twoPower7
    private val invTwoPower15 = 1 / twoPower15
    private val invTwoPower23 = 1 / twoPower23
    private val invTwoPower31 = 1 / twoPower31
    /**
     * @param output          an array of float[] arrays
     * @param allowAddChannel if true, and output has fewer channels than
     * format, then only output.length channels are filled
     * @throws ArrayIndexOutOfBoundsException if output does not
     * format.getChannels() elements
     * @see .byte2float
     */
    /**
     * @param output an array of float[] arrays
     * @throws ArrayIndexOutOfBoundsException if output does not
     * format.getChannels() elements
     * @see .byte2float
     */
    @JvmOverloads
    fun byte2float(
        input: ByteArray, inByteOffset: Int,
        output: Array<FloatArray>, outOffset: Int, frameCount: Int, format: AudioFormat,
        allowAddChannel: Boolean =
            true
    ) {
        var inByteOffset = inByteOffset
        var channels = format.channels
        if (!allowAddChannel && channels > output.size) {
            channels = output.size
        }
        if (output.size < channels) {
            throw ArrayIndexOutOfBoundsException(
                "too few channel output array"
            )
        }
        for (channel in 0 until channels) {
            var data = output[channel] as FloatArray
            if (data.size < frameCount + outOffset) {
                data = FloatArray(frameCount + outOffset)
                output[channel] = data
            }
            byte2floatGeneric(
                input, inByteOffset, format.frameSize, data,
                outOffset, frameCount, format
            )
            inByteOffset += format.frameSize / format.channels
        }
    }
    /**
     * Conversion function to convert an interleaved byte array to a List of
     * interleaved float arrays. The float arrays will contain normalized
     * samples in the range [-1.0, +1.0]. The input array provides bytes in the
     * format specified in `format`.
     *
     *
     * Only PCM formats are accepted. The method will convert all byte values
     * from `input[inByteOffset]` to
     * `input[inByteOffset + (frameCount * format.getFrameSize()) - 1]`
     * to floats from `output(n)[outOffset]` to
     * `output(n)[outOffset + frameCount - 1]`
     *
     * @param input           the audio data in an byte array
     * @param inByteOffset    index in input where to start the conversion
     * @param output          list of float[] arrays which receive the converted audio
     * data. if the list does not contain enough elements, or
     * individual float arrays are not large enough, they are
     * created.
     * @param outOffset       the start offset in `output`
     * @param frameCount      number of frames to be converted
     * @param format          the input format. Only packed PCM is allowed
     * @param allowAddChannel if true, channels may be added to
     * `output` to match the number of input channels,
     * otherwise, only the first output.size() channels of input data
     * are converted.
     * @throws IllegalArgumentException if one of the parameters is out of
     * bounds
     * @see .byte2floatInterleaved
     */
    /**
     * @see .byte2float
     */
    fun byte2float(
        input: ByteArray, inByteOffset: Int,
        output: MutableList<FloatArray>, outOffset: Int, frameCount: Int,
        format: AudioFormat, allowAddChannel: Boolean = true
    ) {
        var inByteOffset = inByteOffset
        var channels = format.channels
        if (!allowAddChannel && channels > output.size) {
            channels = output.size
        }
        for (channel in 0 until channels) {
            var data: FloatArray
            if (output.size < channel) {
                data = FloatArray(frameCount + outOffset)
                output.add(data)
            }
            else {
                data = output[channel]
                if (data.size < frameCount + outOffset) {
                    data = FloatArray(frameCount + outOffset)
                    output[channel] = data
                }
            }
            byte2floatGeneric(
                input, inByteOffset, format.frameSize, data,
                outOffset, frameCount, format
            )
            inByteOffset += format.frameSize / format.channels
        }
    }

    /**
     * Conversion function to convert one audio channel in an interleaved byte
     * array to a float array. The float array will contain normalized samples
     * in the range [-1.0, +1.0]. The input array provides bytes in the format
     * specified in `format`.
     *
     *
     * Only PCM formats are accepted. The method will convert all byte values
     * from `input[inByteOffset]` to
     * `input[inByteOffset + (frameCount * format.getFrameSize()) - 1]`
     * to floats from `output(n)[outOffset]` to
     * `output(n)[outOffset + frameCount - 1]`
     *
     * @param channel      the channel number to extract from the input audio data
     * @param input        the audio data in an byte array
     * @param inByteOffset index in input where to start the conversion
     * @param output       the of float array which receives the converted audio data.
     * @param outOffset    the start offset in `output`
     * @param frameCount   number of frames to be converted
     * @param format       the input format. Only packed PCM is allowed
     * @throws IllegalArgumentException if one of the parameters is out of
     * bounds
     */
    fun byte2float(
        channel: Int, input: ByteArray, inByteOffset: Int,
        output: FloatArray, outOffset: Int, frameCount: Int, format: AudioFormat
    ) {
        var inByteOffset = inByteOffset
        if (channel >= format.channels) {
            throw IllegalArgumentException("channel out of bounds")
        }
        if (output.size < frameCount + outOffset) {
            throw IllegalArgumentException("data is too small")
        }

        // "select" the channel
        inByteOffset += format.frameSize / format.channels * channel
        byte2floatGeneric(
            input, inByteOffset, format.frameSize, output,
            outOffset, frameCount, format
        )
    }

    /**
     * Conversion function to convert an interleaved byte array to an
     * interleaved float array. The float array will contain normalized samples
     * in the range [-1.0f, +1.0f]. The input array provides bytes in the format
     * specified in `format`.
     *
     *
     * Only PCM formats are accepted. The method will convert all byte values
     * from `input[inByteOffset]` to
     * `input[inByteOffset + (frameCount * format.getFrameSize()) - 1]`
     * to floats from `output[outOffset]` to
     * `output[outOffset + (frameCount * format.getChannels()) - 1]`
     *
     * @param input        the audio data in an byte array
     * @param inByteOffset index in input where to start the conversion
     * @param output       the float array that receives the converted audio data
     * @param outOffset    the start offset in `output`
     * @param frameCount   number of frames to be converted
     * @param format       the input format. Only packed PCM is allowed
     * @throws IllegalArgumentException if one of the parameters is out of
     * bounds
     * @see .byte2float
     */
    fun byte2floatInterleaved(
        input: ByteArray, inByteOffset: Int,
        output: FloatArray, outOffset: Int, frameCount: Int, format: AudioFormat
    ) {
        byte2floatGeneric(
            input, inByteOffset, (format.frameSize
                    / format.channels), output, outOffset, (frameCount
                    * format.channels), format
        )
    }

    /**
     * Generic conversion function to convert a byte array to a float array.
     *
     *
     * Only PCM formats are accepted. The method will convert all bytes from
     * `input[inByteOffset]` to
     * `input[inByteOffset + (sampleCount * (inByteStep - 1)]` to
     * samples from `output[outOffset]` to
     * `output[outOffset+sampleCount-1]`.
     *
     *
     * The `format`'s channel count is ignored.
     *
     *
     * For mono data, set `inByteOffset` to
     * `format.getFrameSize()`.<br></br>
     * For converting interleaved input data, multiply `sampleCount`
     * by the number of channels and set inByteStep to
     * `format.getFrameSize() / format.getChannels()`.
     *
     * @param sampleCount number of samples to be written to output
     * @param inByteStep  how many bytes advance for each output sample in
     * `output`.
     * @throws IllegalArgumentException if one of the parameters is out of
     * bounds
     * @see .byte2floatInterleaved
     * @see .byte2float
     */
    fun byte2floatGeneric(
        input: ByteArray, inByteOffset: Int,
        inByteStep: Int, output: FloatArray, outOffset: Int, sampleCount: Int,
        format: AudioFormat
    ) {
        val formatType = getFormatType(format)
        byte2floatGeneric(
            input, inByteOffset, inByteStep, output, outOffset,
            sampleCount, formatType
        )
    }

    /**
     * Central conversion function from a byte array to a normalized float
     * array. In order to accomodate interleaved and non-interleaved samples,
     * this method takes inByteStep as parameter which can be used to flexibly
     * convert the data.
     *
     *
     * E.g.:<br></br>
     * mono->mono: inByteStep=format.getFrameSize()<br></br>
     * interleaved_stereo->interleaved_stereo:
     * inByteStep=format.getFrameSize()/2, sampleCount*2<br></br>
     * stereo->2 mono arrays:<br></br>
     * ---inByteOffset=0, outOffset=0, inByteStep=format.getFrameSize()<br></br>
     * ---inByteOffset=format.getFrameSize()/2, outOffset=1,
     * inByteStep=format.getFrameSize()<br></br>
     */
    fun byte2floatGeneric(
        input: ByteArray, inByteOffset: Int,
        inByteStep: Int, output: FloatArray, outOffset: Int, sampleCount: Int,
        formatType: Int
    ) {
        // if (TDebug.TraceAudioConverter) {
        // TDebug.out("FloatSampleTools.byte2floatGeneric, formatType="
        // +formatType2Str(formatType));
        // }
        val endCount = outOffset + sampleCount
        var inIndex = inByteOffset
        var outIndex = outOffset
        while (outIndex < endCount) {
            when (formatType) {
                CT_8S -> output[outIndex] = input[inIndex] * invTwoPower7
                CT_8U -> output[outIndex] = ((input[inIndex].toInt() and 0xFF) - 128) * invTwoPower7
                CT_16SB -> output[outIndex] = (((input[inIndex].toInt() shl 8)
                        or (input[inIndex + 1].toInt() and 0xFF))
                        * invTwoPower15)

                CT_16SL -> output[outIndex] = (((input[inIndex + 1].toInt() shl 8)
                        or (input[inIndex].toInt() and 0xFF))
                        * invTwoPower15)

                CT_24_3SB -> output[outIndex] = ((((input[inIndex].toInt() shl 16)
                        or ((input[inIndex + 1].toInt() and 0xFF) shl 8)
                        or (input[inIndex + 2].toInt() and 0xFF)))
                        * invTwoPower23)

                CT_24_3SL -> output[outIndex] = ((((input[inIndex + 2].toInt() shl 16)
                        or ((input[inIndex + 1].toInt() and 0xFF) shl 8)
                        or (input[inIndex].toInt() and 0xFF)))
                        * invTwoPower23)

                CT_24_4SB -> output[outIndex] = ((((input[inIndex + 1].toInt() shl 16)
                        or ((input[inIndex + 2].toInt() and 0xFF) shl 8)
                        or (input[inIndex + 3].toInt() and 0xFF)))
                        * invTwoPower23)

                CT_24_4SL ->                     // TODO: verify the indexes
                    output[outIndex] = ((((input[inIndex + 3].toInt() shl 16)
                            or ((input[inIndex + 2].toInt() and 0xFF) shl 8)
                            or (input[inIndex + 1].toInt() and 0xFF)))
                            * invTwoPower23)

                CT_32SB -> output[outIndex] = ((((input[inIndex].toInt() shl 24)
                        or ((input[inIndex + 1].toInt() and 0xFF) shl 16)
                        or ((input[inIndex + 2].toInt() and 0xFF) shl 8)
                        or (input[inIndex + 3].toInt() and 0xFF)))
                        * invTwoPower31)

                CT_32SL -> output[outIndex] = ((((input[inIndex + 3].toInt() shl 24)
                        or ((input[inIndex + 2].toInt() and 0xFF) shl 16)
                        or ((input[inIndex + 1].toInt() and 0xFF) shl 8)
                        or (input[inIndex].toInt() and 0xFF)))
                        * invTwoPower31)

                else -> throw IllegalArgumentException(
                    ("unsupported format="
                            + formatType2Str(formatType))
                )
            }
            outIndex++
            inIndex += inByteStep
        }
    }

    // /////////////////// FLOAT 2 BYTE /////////////////////////////////// //
    private fun quantize8(sample: Float, ditherBits: Float): Byte {
        var sample = sample
        if (ditherBits != 0f) {
            sample += random!!.nextFloat() * ditherBits
        }
        if (sample >= 127.0f) {
            return 127.toByte()
        }
        else return if (sample <= -128.0f) {
            (-128.toByte()).toByte()
        }
        else {
            (if (sample < 0) (sample - 0.5f) else (sample + 0.5f)).toInt().toByte()
        }
    }

    private fun quantize16(sample: Float, ditherBits: Float): Int {
        var sample = sample
        if (ditherBits != 0f) {
            sample += random!!.nextFloat() * ditherBits
        }
        if (sample >= 32767.0f) {
            return 32767
        }
        else return if (sample <= -32768.0f) {
            -32768
        }
        else {
            (if (sample < 0) (sample - 0.5f) else (sample + 0.5f)).toInt()
        }
    }

    private fun quantize24(sample: Float, ditherBits: Float): Int {
        var sample = sample
        if (ditherBits != 0f) {
            sample += random!!.nextFloat() * ditherBits
        }
        if (sample >= 8388607.0f) {
            return 8388607
        }
        else return if (sample <= -8388608.0f) {
            -8388608
        }
        else {
            (if (sample < 0) (sample - 0.5f) else (sample + 0.5f)).toInt()
        }
    }

    private fun quantize32(sample: Float, ditherBits: Float): Int {
        var sample = sample
        if (ditherBits != 0f) {
            sample += random!!.nextFloat() * ditherBits
        }
        if (sample >= 2147483647.0f) {
            return 2147483647
        }
        else return if (sample <= -2147483648.0f) {
            -2147483648
        }
        else {
            (if (sample < 0) (sample - 0.5f) else (sample + 0.5f)).toInt()
        }
    }

    /**
     * Conversion function to convert a non-interleaved float audio data to an
     * interleaved byte array. The float arrays contains normalized samples in
     * the range [-1.0f, +1.0f]. The output array will receive bytes in the
     * format specified in `format`. Exactly
     * `format.getChannels()` channels are converted regardless of
     * the number of elements in `input`. If `input`
     * does not provide enough channels, an `IllegalArgumentException`
     * is thrown.
     *
     *
     * Only PCM formats are accepted. The method will convert all samples from
     * `input(n)[inOffset]` to
     * `input(n)[inOffset + frameCount - 1]` to byte values from
     * `output[outByteOffset]` to
     * `output[outByteOffset + (frameCount * format.getFrameSize()) - 1]`
     *
     *
     * Dithering should be used when the output resolution is significantly
     * lower than the original resolution. This includes if the original data
     * was 16-bit and it is now converted to 8-bit, or if the data was generated
     * in the float domain. No dithering need to be used if the original sample
     * data was in e.g. 8-bit and the resulting output data has a higher
     * resolution. If dithering is used, a sensitive value is
     * DEFAULT_DITHER_BITS.
     *
     * @param input         a List of float arrays with the input audio data
     * @param inOffset      index in the input arrays where to start the conversion
     * @param output        the byte array that receives the converted audio data
     * @param outByteOffset the start offset in `output`
     * @param frameCount    number of frames to be converted.
     * @param format        the output format. Only packed PCM is allowed
     * @param ditherBits    if 0, do not dither. Otherwise the number of bits to be
     * dithered
     * @throws IllegalArgumentException if one of the parameters is out of
     * bounds
     * @see .DEFAULT_DITHER_BITS
     *
     * @see .float2byteInterleaved
     */
    fun float2byte(
        input: List<FloatArray>, inOffset: Int,
        output: ByteArray, outByteOffset: Int, frameCount: Int,
        format: AudioFormat, ditherBits: Float
    ) {
        var outByteOffset = outByteOffset
        for (channel in 0 until format.channels) {
            val data = input[channel]
            float2byteGeneric(
                data, inOffset, output, outByteOffset,
                format.frameSize, frameCount, format, ditherBits
            )
            outByteOffset += format.frameSize / format.channels
        }
    }

    /**
     * @param input an array of float[] arrays
     * @throws ArrayIndexOutOfBoundsException if one of the parameters is out of
     * bounds
     * @see .float2byte
     */
    fun float2byte(
        input: Array<Any>, inOffset: Int, output: ByteArray,
        outByteOffset: Int, frameCount: Int, format: AudioFormat,
        ditherBits: Float
    ) {
        var outByteOffset = outByteOffset
        val channels = format.channels
        for (channel in 0 until channels) {
            val data = input[channel] as FloatArray
            float2byteGeneric(
                data, inOffset, output, outByteOffset,
                format.frameSize, frameCount, format, ditherBits
            )
            outByteOffset += format.frameSize / format.channels
        }
    }

    /**
     * @param input     an array of float[] arrays
     * @param channels  how many channels to use from the input array
     * @param frameSize only as optimization, the number of bytes per sample
     * frame
     * @throws ArrayIndexOutOfBoundsException if one of the parameters is out of
     * bounds
     * @see .float2byte
     */
    fun float2byte(
        input: Array<FloatArray>, inOffset: Int, output: ByteArray,
        outByteOffset: Int, frameCount: Int, formatCode: Int, channels: Int,
        frameSize: Int, ditherBits: Float
    ) {
        var outByteOffset = outByteOffset
        val sampleSize = frameSize / channels
        for (channel in 0 until channels) {
            val data = input[channel]
            float2byteGeneric(
                data, inOffset, output, outByteOffset, frameSize,
                frameCount, formatCode, ditherBits
            )
            outByteOffset += sampleSize
        }
    }

    /**
     * Conversion function to convert an interleaved float array to an
     * interleaved byte array. The float array contains normalized samples in
     * the range [-1.0f, +1.0f]. The output array will receive bytes in the
     * format specified in `format`.
     *
     *
     * Only PCM formats are accepted. The method will convert all samples from
     * `input[inOffset]` to
     * `input[inOffset + (frameCount * format.getChannels()) - 1]`
     * to byte values from `output[outByteOffset]` to
     * `output[outByteOffset + (frameCount * format.getFrameSize()) - 1]`
     *
     *
     * Dithering should be used when the output resolution is significantly
     * lower than the original resolution. This includes if the original data
     * was 16-bit and it is now converted to 8-bit, or if the data was generated
     * in the float domain. No dithering need to be used if the original sample
     * data was in e.g. 8-bit and the resulting output data has a higher
     * resolution. If dithering is used, a sensitive value is
     * DEFAULT_DITHER_BITS.
     *
     * @param input         the audio data in normalized samples
     * @param inOffset      index in input where to start the conversion
     * @param output        the byte array that receives the converted audio data
     * @param outByteOffset the start offset in `output`
     * @param frameCount    number of frames to be converted.
     * @param format        the output format. Only packed PCM is allowed
     * @param ditherBits    if 0, do not dither. Otherwise the number of bits to be
     * dithered
     * @throws IllegalArgumentException if one of the parameters is out of
     * bounds
     * @see .DEFAULT_DITHER_BITS
     *
     * @see .float2byte
     */
    fun float2byteInterleaved(
        input: FloatArray, inOffset: Int,
        output: ByteArray, outByteOffset: Int, frameCount: Int,
        format: AudioFormat, ditherBits: Float
    ) {
        float2byteGeneric(
            input, inOffset, output, outByteOffset,
            format.frameSize / format.channels, (frameCount
                    * format.channels), format, ditherBits
        )
    }

    /**
     * Generic conversion function to convert a float array to a byte array.
     *
     *
     * Only PCM formats are accepted. The method will convert all samples from
     * `input[inOffset]` to
     * `input[inOffset+sampleCount-1]` to byte values from
     * `output[outByteOffset]` to
     * `output[outByteOffset + (sampleCount * (outByteStep - 1)]`.
     *
     *
     * The `format`'s channel count is ignored.
     *
     *
     * For mono data, set `outByteOffset` to
     * `format.getFrameSize()`.<br></br>
     * For converting interleaved input data, multiply `sampleCount`
     * by the number of channels and set outByteStep to
     * `format.getFrameSize() / format.getChannels()`.
     *
     * @param sampleCount number of samples in input to be converted.
     * @param outByteStep how many bytes advance for each input sample in
     * `input`.
     * @throws IllegalArgumentException if one of the parameters is out of
     * bounds
     * @see .float2byteInterleaved
     * @see .float2byte
     */
    fun float2byteGeneric(
        input: FloatArray, inOffset: Int, output: ByteArray,
        outByteOffset: Int, outByteStep: Int, sampleCount: Int,
        format: AudioFormat, ditherBits: Float
    ) {
        val formatType = getFormatType(format)
        float2byteGeneric(
            input, inOffset, output, outByteOffset, outByteStep,
            sampleCount, formatType, ditherBits
        )
    }

    /**
     * Central conversion function from normalized float array to a byte array.
     * In order to accomodate interleaved and non-interleaved samples, this
     * method takes outByteStep as parameter which can be used to flexibly
     * convert the data.
     *
     *
     * E.g.:<br></br>
     * mono->mono: outByteStep=format.getFrameSize()<br></br>
     * interleaved stereo->interleaved stereo:
     * outByteStep=format.getFrameSize()/2, sampleCount*2<br></br>
     * 2 mono arrays->stereo:<br></br>
     * ---inOffset=0, outByteOffset=0, outByteStep=format.getFrameSize()<br></br>
     * ---inOffset=1, outByteOffset=format.getFrameSize()/2,
     * outByteStep=format.getFrameSize()<br></br>
     */
    fun float2byteGeneric(
        input: FloatArray, inOffset: Int, output: ByteArray,
        outByteOffset: Int, outByteStep: Int, sampleCount: Int,
        formatType: Int, ditherBits: Float
    ) {
        // if (TDebug.TraceAudioConverter) {
        // TDebug.out("FloatSampleBuffer.float2byteGeneric, formatType="
        // +"formatType2Str(formatType));
        // }
        if ((inOffset < 0) || (inOffset + sampleCount > input.size
                    ) || (sampleCount < 0)
        ) {
            throw IllegalArgumentException(
                ("invalid input index: "
                        + "input.length=" + input.size + " inOffset=" + inOffset
                        + " sampleCount=" + sampleCount)
            )
        }
        if ((outByteOffset < 0
                    ) || (outByteOffset + (sampleCount * outByteStep) >= (output.size + outByteStep)
                    ) || (outByteStep < getSampleSize(formatType))
        ) {
            throw IllegalArgumentException(
                ("invalid output index: "
                        + "output.length=" + output.size + " outByteOffset="
                        + outByteOffset + " outByteStep=" + outByteStep
                        + " sampleCount=" + sampleCount + " format="
                        + formatType2Str(formatType))
            )
        }
        if (ditherBits != 0.0f && random == null) {
            // create the random number generator for dithering
            random = Random()
        }
        val endSample = inOffset + sampleCount
        var iSample: Int
        var outIndex = outByteOffset
        var inIndex = inOffset
        while (inIndex < endSample) {
            when (formatType) {
                CT_8S -> output[outIndex] = quantize8(
                    input[inIndex] * twoPower7,
                    ditherBits
                )

                CT_8U -> output[outIndex] = (quantize8(
                    (input[inIndex] * twoPower7), ditherBits
                ) + 128).toByte()

                CT_16SB -> {
                    iSample = quantize16(input[inIndex] * twoPower15, ditherBits)
                    output[outIndex] = (iSample shr 8).toByte()
                    output[outIndex + 1] = (iSample and 0xFF).toByte()
                }

                CT_16SL -> {
                    iSample = quantize16(input[inIndex] * twoPower15, ditherBits)
                    output[outIndex + 1] = (iSample shr 8).toByte()
                    output[outIndex] = (iSample and 0xFF).toByte()
                }

                CT_24_3SB -> {
                    iSample = quantize24(input[inIndex] * twoPower23, ditherBits)
                    output[outIndex] = (iSample shr 16).toByte()
                    output[outIndex + 1] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex + 2] = (iSample and 0xFF).toByte()
                }

                CT_24_3SL -> {
                    iSample = quantize24(input[inIndex] * twoPower23, ditherBits)
                    output[outIndex + 2] = (iSample shr 16).toByte()
                    output[outIndex + 1] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex] = (iSample and 0xFF).toByte()
                }

                CT_24_4SB -> {
                    // TODO: verify
                    iSample = quantize24(input[inIndex] * twoPower23, ditherBits)
                    output[outIndex] = 0
                    output[outIndex + 1] = (iSample shr 16).toByte()
                    output[outIndex + 2] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex + 3] = (iSample and 0xFF).toByte()
                }

                CT_24_4SL -> {
                    // TODO: verify
                    iSample = quantize24(input[inIndex] * twoPower23, ditherBits)
                    output[outIndex + 3] = (iSample shr 16).toByte()
                    output[outIndex + 2] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex + 1] = (iSample and 0xFF).toByte()
                    output[outIndex] = 0
                }

                CT_32SB -> {
                    iSample = quantize32(input[inIndex] * twoPower31, ditherBits)
                    output[outIndex] = (iSample shr 24).toByte()
                    output[outIndex + 1] = ((iSample ushr 16) and 0xFF).toByte()
                    output[outIndex + 2] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex + 3] = (iSample and 0xFF).toByte()
                }

                CT_32SL -> {
                    iSample = quantize32(input[inIndex] * twoPower31, ditherBits)
                    output[outIndex + 3] = (iSample shr 24).toByte()
                    output[outIndex + 2] = ((iSample ushr 16) and 0xFF).toByte()
                    output[outIndex + 1] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex] = (iSample and 0xFF).toByte()
                }

                else -> throw IllegalArgumentException(
                    ("unsupported format="
                            + formatType2Str(formatType))
                )
            }
            inIndex++
            outIndex += outByteStep
        }
    }
}

