package de.visualdigits.kotlin.minim.buffer

import java.lang.Math.pow
import java.util.Random
import javax.sound.sampled.AudioFormat
import kotlin.math.pow

/**
 * Utility functions for handling data in normalized double arrays. Each sample
 * is linear in the range of [-1, +1].
 * <p>
 * Currently, the following bit sizes are supported:
 * <ul>
 * <li>8-bit
 * <li>16-bit
 * <li>packed 24-bit (stored in 3 bytes)
 * <li>unpacked 24-bit (stored in 4 bytes)
 * <li>32-bit
 * </ul>
 * 8-bit data can be unsigned or signed. All other data is only supported in
 * signed encoding.
 *
 * @author Florian Bomers (java version)
 */
object DoubleSampleTools {

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

    private var random: Random? = null

    /**
     * Generic conversion function to convert a byte array to a double array.
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
     */
    fun byte2doubleGeneric(
        input: ByteArray, inByteOffset: Int,
        inByteStep: Int, output: DoubleArray, outOffset: Int, sampleCount: Int,
        format: AudioFormat
    ) {
        val formatType = getFormatType(format)
        byte2doubleGeneric(
            input, inByteOffset, inByteStep, output, outOffset,
            sampleCount, formatType
        )
    }

    /**
     * Get the formatType code from the given format.
     */
    fun getFormatType(format: AudioFormat): Int {
        val signed = format.encoding ==
                AudioFormat.Encoding.PCM_SIGNED
        if (!signed
            && format.encoding != AudioFormat.Encoding.PCM_UNSIGNED
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
     * only allow "packed" samples -- currently no support for 18, 20 bits --
     * except 24 bits stored in 4 bytes.
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

    /**
     * Central conversion function from a byte array to a normalized double
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
    fun byte2doubleGeneric(
        input: ByteArray, inByteOffset: Int,
        inByteStep: Int, output: DoubleArray, outOffset: Int, sampleCount: Int,
        formatType: Int
    ) {
        val endCount = outOffset + sampleCount
        var inIndex = inByteOffset
        var outIndex = outOffset
        while (outIndex < endCount) {
            when (formatType) {
                CT_8S -> output[outIndex] = input[inIndex] * (1 / pow(2.0, 7.0))
                CT_8U -> output[outIndex] = ((input[inIndex].toInt() and 0xFF) - 128) * (1 / 2.0.pow(7.0))
                CT_16SB -> output[outIndex] = (((input[inIndex].toInt() shl 8)
                        or (input[inIndex + 1].toInt() and 0xFF))
                        * (1 / 2.0.pow(15.0)))

                CT_16SL -> output[outIndex] = (((input[inIndex + 1].toInt() shl 8)
                        or (input[inIndex].toInt() and 0xFF))
                        * (1 / 2.0.pow(15.0)))

                CT_24_3SB -> output[outIndex] = ((((input[inIndex].toInt() shl 16)
                        or ((input[inIndex + 1].toInt() and 0xFF) shl 8)
                        or (input[inIndex + 2].toInt() and 0xFF)))
                        * (1 / 2.0.pow(23.0)))

                CT_24_3SL -> output[outIndex] = ((((input[inIndex + 2].toInt() shl 16)
                        or ((input[inIndex + 1].toInt() and 0xFF) shl 8)
                        or (input[inIndex].toInt() and 0xFF)))
                        * (1 / 2.0.pow(23.0)))

                CT_24_4SB -> output[outIndex] = ((((input[inIndex + 1].toInt() shl 16)
                        or ((input[inIndex + 2].toInt() and 0xFF) shl 8)
                        or (input[inIndex + 3].toInt() and 0xFF)))
                        * (1 / 2.0.pow(23.0)))

                CT_24_4SL -> output[outIndex] = ((((input[inIndex + 3].toInt() shl 16)
                        or ((input[inIndex + 2].toInt() and 0xFF) shl 8)
                        or (input[inIndex + 1].toInt() and 0xFF)))
                        * (1 / 2.0.pow(23.0)))

                CT_32SB -> output[outIndex] = ((((input[inIndex].toInt() shl 24)
                        or ((input[inIndex + 1].toInt() and 0xFF) shl 16)
                        or ((input[inIndex + 2].toInt() and 0xFF) shl 8)
                        or (input[inIndex + 3].toInt() and 0xFF)))
                        * (1 / 2.0.pow(31.0)))

                CT_32SL -> output[outIndex] = ((((input[inIndex + 3].toInt() shl 24)
                        or ((input[inIndex + 2].toInt() and 0xFF) shl 16)
                        or ((input[inIndex + 1].toInt() and 0xFF) shl 8)
                        or (input[inIndex].toInt() and 0xFF)))
                        * (1 / 2.0.pow(31.0)))

                else -> throw IllegalArgumentException(
                    ("unsupported format="
                            + formatType2Str(formatType))
                )
            }
            outIndex++
            inIndex += inByteStep
        }
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

    /**
     * @param output          an array of double[] arrays
     * @param allowAddChannel if true, and output has fewer channels than
     * format, then only output.length channels are filled
     * @throws ArrayIndexOutOfBoundsException if output does not
     * format.getChannels() elements
     */
    fun byte2double(
        input: ByteArray, inByteOffset: Int,
        output: MutableList<DoubleArray>, outOffset: Int, frameCount: Int, format: AudioFormat,
        allowAddChannel: Boolean
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
            var data = output[channel]
            if (data.size < frameCount + outOffset) {
                data = DoubleArray(frameCount + outOffset)
                output[channel] = data
            }
            byte2doubleGeneric(
                input, inByteOffset, format.frameSize, data,
                outOffset, frameCount, format
            )
            inByteOffset += format.frameSize / format.channels
        }
    }

    /**
     * Central conversion function from normalized double array to a byte array.
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
    fun double2byteGeneric(
        input: DoubleArray, output: ByteArray,
        outByteOffset: Int, outByteStep: Int, sampleCount: Int,
        formatType: Int, ditherBits: Double
    ) {
        if ((sampleCount > input.size
                    || sampleCount < 0)
        ) {
            throw IllegalArgumentException(
                ("invalid input index: "
                        + "input.length=" + input.size
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
        if (ditherBits != 0.0 && random == null) {
            // create the random number generator for dithering
            random = Random()
        }
        var iSample: Int
        var outIndex = outByteOffset
        var inIndex = 0
        while (inIndex < sampleCount) {
            when (formatType) {
                CT_8S -> output[outIndex] = quantize(input[inIndex], ditherBits, 2.0.pow(7.0))
                CT_8U -> output[outIndex] =
                    (quantize(input[inIndex], ditherBits, 2.0.pow(7.0)) + 2.0.pow(7.0)).toInt().toByte()

                CT_16SB -> {
                    iSample = quantize(input[inIndex], ditherBits, 2.0.pow(15.0)).toInt()
                    output[outIndex] = (iSample shr 8).toByte()
                    output[outIndex + 1] = (iSample and 0xFF).toByte()
                }

                CT_16SL -> {
                    iSample = quantize(input[inIndex], ditherBits, 2.0.pow(15.0)).toInt()
                    output[outIndex + 1] = (iSample shr 8).toByte()
                    output[outIndex] = (iSample and 0xFF).toByte()
                }

                CT_24_3SB -> {
                    iSample = quantize(input[inIndex], ditherBits, 2.0.pow(23.0)).toInt()
                    output[outIndex] = (iSample shr 16).toByte()
                    output[outIndex + 1] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex + 2] = (iSample and 0xFF).toByte()
                }

                CT_24_3SL -> {
                    iSample = quantize(input[inIndex], ditherBits, 2.0.pow(23.0)).toInt()
                    output[outIndex + 2] = (iSample shr 16).toByte()
                    output[outIndex + 1] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex] = (iSample and 0xFF).toByte()
                }

                CT_24_4SB -> {
                    iSample = quantize(input[inIndex], ditherBits, 2.0.pow(23.0)).toInt()
                    output[outIndex] = 0
                    output[outIndex + 1] = (iSample shr 16).toByte()
                    output[outIndex + 2] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex + 3] = (iSample and 0xFF).toByte()
                }

                CT_24_4SL -> {
                    iSample = quantize(input[inIndex], ditherBits, 2.0.pow(23.0)).toInt()
                    output[outIndex + 3] = (iSample shr 16).toByte()
                    output[outIndex + 2] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex + 1] = (iSample and 0xFF).toByte()
                    output[outIndex] = 0
                }

                CT_32SB -> {
                    iSample = quantize(input[inIndex], ditherBits, 2.0.pow(31.0)).toInt()
                    output[outIndex] = (iSample shr 24).toByte()
                    output[outIndex + 1] = ((iSample ushr 16) and 0xFF).toByte()
                    output[outIndex + 2] = ((iSample ushr 8) and 0xFF).toByte()
                    output[outIndex + 3] = (iSample and 0xFF).toByte()
                }

                CT_32SL -> {
                    iSample = quantize(input[inIndex], ditherBits, 2.0.pow(31.0)).toInt()
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

    fun getSampleSize(formatType: Int): Int {
        return when (formatType and F_SAMPLE_WIDTH_MASK) {
            F_8 -> 1
            F_16 -> 2
            F_24_3 -> 3
            F_24_4, F_32 -> 4
            else -> 0
        }
    }

    private fun quantize(sample: Double, ditherBits: Double, n: Double): Byte {
        var sample = sample
        sample *= n
        if (ditherBits != 0.0) {
            sample += random!!.nextFloat() * ditherBits
        }
        if (sample >= n - 1) {
            return (n - 1).toInt().toByte()
        }
        else {
            return if (sample <= -n) {
                (-n.toInt().toByte()).toByte()
            }
            else {
                (if (sample < 0) (sample - 0.5) else (sample + 0.5)).toInt().toByte()
            }
        }
    }

    /**
     * @param input     an array of double[] arrays
     * @param channels  how many channels to use from the input array
     * @param frameSize only as optimization, the number of bytes per sample
     * frame
     * @throws ArrayIndexOutOfBoundsException if one of the parameters is out of
     * bounds
     */
    fun double2byte(
        input: List<DoubleArray>, output: ByteArray,
        outByteOffset: Int, frameCount: Int, formatCode: Int, channels: Int,
        frameSize: Int, ditherBits: Double
    ) {
        var outByteOffset = outByteOffset
        val sampleSize = frameSize / channels
        for (channel in 0 until channels) {
            val data = input[channel]
            double2byteGeneric(
                data, output, outByteOffset, frameSize,
                frameCount, formatCode, ditherBits
            )
            outByteOffset += sampleSize
        }
    }
}
