/*
 * FloatSampleTools.java
 *
 *	This file is part of Tritonus: http://www.tritonus.org/
 */

/*
 *  Copyright (c) 2000-2006 by Florian Bomers <http://www.bomers.de>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 |<---            this code is formatted to fit into 80 columns             --->|
 */

package de.visualdigits.minim.javasound;

import javax.sound.sampled.AudioFormat;
import java.util.List;
import java.util.Random;


/**
 * Utility functions for handling data in normalized double arrays. Each sample
 * is linear in the range of [-1.0f, +1.0f].
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
 * @author Florian Bomers
 * @see DoubleSampleBuffer
 */

public class DoubleSampleTools {

    /**
     * default number of bits to be dithered: 0.7f
     */
    public static final double DEFAULT_DITHER_BITS = 0.7f;
    // sample width (must be in order !)
    static final int F_8 = 1;
    static final int F_16 = 2;
    static final int F_24_3 = 3;
    static final int F_24_4 = 4;
    static final int F_32 = 5;
    static final int F_SAMPLE_WIDTH_MASK = F_8 | F_16 | F_24_3 | F_24_4 | F_32;
    // format bit-flags
    static final int F_SIGNED = 8;
    static final int F_BIGENDIAN = 16;
    // supported formats
    static final int CT_8S = F_8 | F_SIGNED;
    static final int CT_8U = F_8;
    static final int CT_16SB = F_16 | F_SIGNED | F_BIGENDIAN;
    static final int CT_16SL = F_16 | F_SIGNED;
    static final int CT_24_3SB = F_24_3 | F_SIGNED | F_BIGENDIAN;
    static final int CT_24_3SL = F_24_3 | F_SIGNED;
    static final int CT_24_4SB = F_24_4 | F_SIGNED | F_BIGENDIAN;
    static final int CT_24_4SL = F_24_4 | F_SIGNED;
    static final int CT_32SB = F_32 | F_SIGNED | F_BIGENDIAN;
    static final int CT_32SL = F_32 | F_SIGNED;
    private static final double twoPower7 = Math.pow(2, 7);
    private static final double twoPower15 = Math.pow(2, 15);
    private static final double twoPower23 = Math.pow(2, 23);
    private static final double twoPower31 = Math.pow(2, 31);
    private static final double invTwoPower7 = 1 / twoPower7;
    private static final double invTwoPower15 = 1 / twoPower15;
    private static final double invTwoPower23 = 1 / twoPower23;
    private static final double invTwoPower31 = 1 / twoPower31;
    private static Random random = null;

    /**
     * Generic conversion function to convert a byte array to a double array.
     * <p>
     * Only PCM formats are accepted. The method will convert all bytes from
     * <code>input[inByteOffset]</code> to
     * <code>input[inByteOffset + (sampleCount * (inByteStep - 1)]</code> to
     * samples from <code>output[outOffset]</code> to
     * <code>output[outOffset+sampleCount-1]</code>.
     * <p>
     * The <code>format</code>'s channel count is ignored.
     * <p>
     * For mono data, set <code>inByteOffset</code> to
     * <code>format.getFrameSize()</code>.<br>
     * For converting interleaved input data, multiply <code>sampleCount</code>
     * by the number of channels and set inByteStep to
     * <code>format.getFrameSize() / format.getChannels()</code>.
     *
     * @param sampleCount number of samples to be written to output
     * @param inByteStep  how many bytes advance for each output sample in
     *                    <code>output</code>.
     * @throws IllegalArgumentException if one of the parameters is out of
     *                                  bounds
     */
    static void byte2doubleGeneric(byte[] input, int inByteOffset,
                                  int inByteStep, double[] output, int outOffset, int sampleCount,
                                  AudioFormat format) {
        int formatType = getFormatType(format);

        byte2doubleGeneric(input, inByteOffset, inByteStep, output, outOffset,
                sampleCount, formatType);
    }

    /**
     * Get the formatType code from the given format.
     *
     * @throws IllegalArgumentException
     */
    static int getFormatType(AudioFormat format) {
        boolean signed = format.getEncoding().equals(
                AudioFormat.Encoding.PCM_SIGNED);
        if (!signed
                && !format.getEncoding().equals(
                AudioFormat.Encoding.PCM_UNSIGNED)) {
            throw new IllegalArgumentException(
                    "unsupported encoding: only PCM encoding supported.");
        }
        if (!signed && format.getSampleSizeInBits() != 8) {
            throw new IllegalArgumentException(
                    "unsupported encoding: only 8-bit can be unsigned");
        }
        checkSupportedSampleSize(format.getSampleSizeInBits(),
                format.getChannels(), format.getFrameSize());

        int formatType = getFormatType(format.getSampleSizeInBits(),
                format.getFrameSize() / format.getChannels(), signed,
                format.isBigEndian());
        return formatType;
    }

    /**
     * only allow "packed" samples -- currently no support for 18, 20 bits --
     * except 24 bits stored in 4 bytes.
     *
     * @throws IllegalArgumentException
     */
    static void checkSupportedSampleSize(int ssib, int channels, int frameSize) {
        if (ssib == 24 && frameSize == 4 * channels) {
            // 24 bits stored in 4 bytes is OK (24_4)
            return;
        }
        if ((ssib * channels) != frameSize * 8) {
            throw new IllegalArgumentException("unsupported sample size: "
                    + ssib + " bits stored in " + (frameSize / channels)
                    + " bytes.");
        }
    }

    // /////////////////// BYTE 2 FLOAT /////////////////////////////////// //

    /**
     * @throws IllegalArgumentException
     */
    static int getFormatType(int ssib, int bytesPerSample, boolean signed,
                             boolean bigEndian) {
        int res = 0;
        if (ssib == 24 || (bytesPerSample == ssib / 8)) {
            if (ssib == 8) {
                res = F_8;
            } else if (ssib == 16) {
                res = F_16;
            } else if (ssib == 24) {
                if (bytesPerSample == 3) {
                    res = F_24_3;
                } else if (bytesPerSample == 4) {
                    res = F_24_4;
                }
            } else if (ssib == 32) {
                res = F_32;
            }
        }
        if (res == 0) {
            throw new IllegalArgumentException(
                    "ConversionTool: unsupported sample size of " + ssib
                            + " bits per sample in " + bytesPerSample
                            + " bytes.");
        }
        if (!signed && bytesPerSample > 1) {
            throw new IllegalArgumentException(
                    "ConversionTool: unsigned samples larger than "
                            + "8 bit are not supported");
        }
        if (signed) {
            res |= F_SIGNED;
        }
        if (bigEndian && (ssib != 8)) {
            res |= F_BIGENDIAN;
        }
        return res;
    }

    /**
     * Central conversion function from a byte array to a normalized double
     * array. In order to accomodate interleaved and non-interleaved samples,
     * this method takes inByteStep as parameter which can be used to flexibly
     * convert the data.
     * <p>
     * E.g.:<br>
     * mono->mono: inByteStep=format.getFrameSize()<br>
     * interleaved_stereo->interleaved_stereo:
     * inByteStep=format.getFrameSize()/2, sampleCount*2<br>
     * stereo->2 mono arrays:<br>
     * ---inByteOffset=0, outOffset=0, inByteStep=format.getFrameSize()<br>
     * ---inByteOffset=format.getFrameSize()/2, outOffset=1,
     * inByteStep=format.getFrameSize()<br>
     */
    static void byte2doubleGeneric(byte[] input, int inByteOffset,
                                  int inByteStep, double[] output, int outOffset, int sampleCount,
                                  int formatType) {
        int endCount = outOffset + sampleCount;
        int inIndex = inByteOffset;
        for (int outIndex = outOffset; outIndex < endCount; outIndex++, inIndex += inByteStep) {
            // do conversion
            switch (formatType) {
                case CT_8S:
                    output[outIndex] = input[inIndex] * invTwoPower7;
                    break;
                case CT_8U:
                    output[outIndex] = ((input[inIndex] & 0xFF) - 128) * invTwoPower7;
                    break;
                case CT_16SB:
                    output[outIndex] = ((input[inIndex] << 8)
                            | (input[inIndex + 1] & 0xFF))
                            * invTwoPower15;
                    break;
                case CT_16SL:
                    output[outIndex] = ((input[inIndex + 1] << 8)
                            | (input[inIndex] & 0xFF))
                            * invTwoPower15;
                    break;
                case CT_24_3SB:
                    output[outIndex] = ((input[inIndex] << 16)
                            | ((input[inIndex + 1] & 0xFF) << 8)
                            | (input[inIndex + 2] & 0xFF))
                            * invTwoPower23;
                    break;
                case CT_24_3SL:
                    output[outIndex] = ((input[inIndex + 2] << 16)
                            | ((input[inIndex + 1] & 0xFF) << 8)
                            | (input[inIndex] & 0xFF))
                            * invTwoPower23;
                    break;
                case CT_24_4SB:
                    output[outIndex] = ((input[inIndex + 1] << 16)
                            | ((input[inIndex + 2] & 0xFF) << 8)
                            | (input[inIndex + 3] & 0xFF))
                            * invTwoPower23;
                    break;
                case CT_24_4SL:
                    // TODO: verify the indexes
                    output[outIndex] = ((input[inIndex + 3] << 16)
                            | ((input[inIndex + 2] & 0xFF) << 8)
                            | (input[inIndex + 1] & 0xFF))
                            * invTwoPower23;
                    break;
                case CT_32SB:
                    output[outIndex] = ((input[inIndex] << 24)
                            | ((input[inIndex + 1] & 0xFF) << 16)
                            | ((input[inIndex + 2] & 0xFF) << 8)
                            | (input[inIndex + 3] & 0xFF))
                            * invTwoPower31;
                    break;
                case CT_32SL:
                    output[outIndex] = ((input[inIndex + 3] << 24)
                            | ((input[inIndex + 2] & 0xFF) << 16)
                            | ((input[inIndex + 1] & 0xFF) << 8)
                            | (input[inIndex] & 0xFF))
                            * invTwoPower31;
                    break;
                default:
                    throw new IllegalArgumentException("unsupported format="
                            + formatType2Str(formatType));
            }
        }
    }

    /**
     * Return a string representation of this format
     */
    static String formatType2Str(int formatType) {
        String res = formatType + ": ";
        switch (formatType & F_SAMPLE_WIDTH_MASK) {
            case F_8:
                res += "8bit";
                break;
            case F_16:
                res += "16bit";
                break;
            case F_24_3:
                res += "24_3bit";
                break;
            case F_24_4:
                res += "24_4bit";
                break;
            case F_32:
                res += "32bit";
                break;
        }
        res += ((formatType & F_SIGNED) == F_SIGNED) ? " signed" : " unsigned";
        if ((formatType & F_SAMPLE_WIDTH_MASK) != F_8) {
            res += ((formatType & F_BIGENDIAN) == F_BIGENDIAN) ? " big endian"
                    : " little endian";
        }
        return res;
    }

    /**
     * @param output an array of double[] arrays
     * @throws ArrayIndexOutOfBoundsException if output does not
     *                                        format.getChannels() elements
     */
    public static void byte2double(byte[] input, int inByteOffset,
                                  List<double[]> output, int outOffset, int frameCount, AudioFormat format) {

        byte2double(input, inByteOffset, output, outOffset, frameCount, format,
                true);
    }

    /**
     * @param output          an array of double[] arrays
     * @param allowAddChannel if true, and output has fewer channels than
     *                        format, then only output.length channels are filled
     * @throws ArrayIndexOutOfBoundsException if output does not
     *                                        format.getChannels() elements
     */
    public static void byte2double(byte[] input, int inByteOffset,
                                  List<double[]> output, int outOffset, int frameCount, AudioFormat format,
                                  boolean allowAddChannel) {

        int channels = format.getChannels();
        if (!allowAddChannel && channels > output.size()) {
            channels = output.size();
        }
        if (output.size() < channels) {
            throw new ArrayIndexOutOfBoundsException(
                    "too few channel output array");
        }
        for (int channel = 0; channel < channels; channel++) {
            double[] data = output.get(channel);
            if (data.length < frameCount + outOffset) {
                data = new double[frameCount + outOffset];
                output.set(channel, data);
            }

            byte2doubleGeneric(input, inByteOffset, format.getFrameSize(), data,
                    outOffset, frameCount, format);
            inByteOffset += format.getFrameSize() / format.getChannels();
        }
    }

    /**
     * Central conversion function from normalized double array to a byte array.
     * In order to accomodate interleaved and non-interleaved samples, this
     * method takes outByteStep as parameter which can be used to flexibly
     * convert the data.
     * <p>
     * E.g.:<br>
     * mono->mono: outByteStep=format.getFrameSize()<br>
     * interleaved stereo->interleaved stereo:
     * outByteStep=format.getFrameSize()/2, sampleCount*2<br>
     * 2 mono arrays->stereo:<br>
     * ---inOffset=0, outByteOffset=0, outByteStep=format.getFrameSize()<br>
     * ---inOffset=1, outByteOffset=format.getFrameSize()/2,
     * outByteStep=format.getFrameSize()<br>
     */
    static void double2byteGeneric(double[] input, int inOffset, byte[] output,
                                  int outByteOffset, int outByteStep, int sampleCount,
                                  int formatType, double ditherBits) {
        if (inOffset < 0 || inOffset + sampleCount > input.length
                || sampleCount < 0) {
            throw new IllegalArgumentException("invalid input index: "
                    + "input.length=" + input.length + " inOffset=" + inOffset
                    + " sampleCount=" + sampleCount);
        }
        if (outByteOffset < 0
                || outByteOffset + (sampleCount * outByteStep) >= (output.length + outByteStep)
                || outByteStep < getSampleSize(formatType)) {
            throw new IllegalArgumentException("invalid output index: "
                    + "output.length=" + output.length + " outByteOffset="
                    + outByteOffset + " outByteStep=" + outByteStep
                    + " sampleCount=" + sampleCount + " format="
                    + formatType2Str(formatType));
        }

        if (ditherBits != 0.0f && random == null) {
            // create the random number generator for dithering
            random = new Random();
        }
        int endSample = inOffset + sampleCount;
        int iSample;
        int outIndex = outByteOffset;
        for (int inIndex = inOffset; inIndex < endSample; inIndex++, outIndex += outByteStep) {
            // do conversion
            switch (formatType) {
                case CT_8S:
                    output[outIndex] = quantize8(input[inIndex] * twoPower7,
                            ditherBits);
                    break;
                case CT_8U:
                    output[outIndex] = (byte) (quantize8(
                            (input[inIndex] * twoPower7), ditherBits) + 128);
                    break;
                case CT_16SB:
                    iSample = quantize16(input[inIndex] * twoPower15, ditherBits);
                    output[outIndex] = (byte) (iSample >> 8);
                    output[outIndex + 1] = (byte) (iSample & 0xFF);
                    break;
                case CT_16SL:
                    iSample = quantize16(input[inIndex] * twoPower15, ditherBits);
                    output[outIndex + 1] = (byte) (iSample >> 8);
                    output[outIndex] = (byte) (iSample & 0xFF);
                    break;
                case CT_24_3SB:
                    iSample = quantize24(input[inIndex] * twoPower23, ditherBits);
                    output[outIndex] = (byte) (iSample >> 16);
                    output[outIndex + 1] = (byte) ((iSample >>> 8) & 0xFF);
                    output[outIndex + 2] = (byte) (iSample & 0xFF);
                    break;
                case CT_24_3SL:
                    iSample = quantize24(input[inIndex] * twoPower23, ditherBits);
                    output[outIndex + 2] = (byte) (iSample >> 16);
                    output[outIndex + 1] = (byte) ((iSample >>> 8) & 0xFF);
                    output[outIndex] = (byte) (iSample & 0xFF);
                    break;
                case CT_24_4SB:
                    // TODO: verify
                    iSample = quantize24(input[inIndex] * twoPower23, ditherBits);
                    output[outIndex] = 0;
                    output[outIndex + 1] = (byte) (iSample >> 16);
                    output[outIndex + 2] = (byte) ((iSample >>> 8) & 0xFF);
                    output[outIndex + 3] = (byte) (iSample & 0xFF);
                    break;
                case CT_24_4SL:
                    // TODO: verify
                    iSample = quantize24(input[inIndex] * twoPower23, ditherBits);
                    output[outIndex + 3] = (byte) (iSample >> 16);
                    output[outIndex + 2] = (byte) ((iSample >>> 8) & 0xFF);
                    output[outIndex + 1] = (byte) (iSample & 0xFF);
                    output[outIndex] = 0;
                    break;
                case CT_32SB:
                    iSample = quantize32(input[inIndex] * twoPower31, ditherBits);
                    output[outIndex] = (byte) (iSample >> 24);
                    output[outIndex + 1] = (byte) ((iSample >>> 16) & 0xFF);
                    output[outIndex + 2] = (byte) ((iSample >>> 8) & 0xFF);
                    output[outIndex + 3] = (byte) (iSample & 0xFF);
                    break;
                case CT_32SL:
                    iSample = quantize32(input[inIndex] * twoPower31, ditherBits);
                    output[outIndex + 3] = (byte) (iSample >> 24);
                    output[outIndex + 2] = (byte) ((iSample >>> 16) & 0xFF);
                    output[outIndex + 1] = (byte) ((iSample >>> 8) & 0xFF);
                    output[outIndex] = (byte) (iSample & 0xFF);
                    break;
                default:
                    throw new IllegalArgumentException("unsupported format="
                            + formatType2Str(formatType));
            }
        }
    }

    static int getSampleSize(int formatType) {
        switch (formatType & F_SAMPLE_WIDTH_MASK) {
            case F_8:
                return 1;
            case F_16:
                return 2;
            case F_24_3:
                return 3;
            case F_24_4:
                return 4;
            case F_32:
                return 4;
        }
        return 0;
    }

    private static byte quantize8(double sample, double ditherBits) {
        if (ditherBits != 0) {
            sample += random.nextFloat() * ditherBits;
        }
        if (sample >= 127.0f) {
            return (byte) 127;
        } else if (sample <= -128.0f) {
            return (byte) -128;
        } else {
            return (byte) (sample < 0 ? (sample - 0.5f) : (sample + 0.5f));
        }
    }

    private static int quantize16(double sample, double ditherBits) {
        if (ditherBits != 0) {
            sample += random.nextFloat() * ditherBits;
        }
        if (sample >= 32767.0f) {
            return 32767;
        } else if (sample <= -32768.0f) {
            return -32768;
        } else {
            return (int) (sample < 0 ? (sample - 0.5f) : (sample + 0.5f));
        }
    }

    private static int quantize24(double sample, double ditherBits) {
        if (ditherBits != 0) {
            sample += random.nextFloat() * ditherBits;
        }
        if (sample >= 8388607.0f) {
            return 8388607;
        } else if (sample <= -8388608.0f) {
            return -8388608;
        } else {
            return (int) (sample < 0 ? (sample - 0.5f) : (sample + 0.5f));
        }
    }

    private static int quantize32(double sample, double ditherBits) {
        if (ditherBits != 0) {
            sample += random.nextFloat() * ditherBits;
        }
        if (sample >= 2147483647.0f) {
            return 2147483647;
        } else if (sample <= -2147483648.0f) {
            return -2147483648;
        } else {
            return (int) (sample < 0 ? (sample - 0.5f) : (sample + 0.5f));
        }
    }

    /**
     * @param input     an array of double[] arrays
     * @param channels  how many channels to use from the input array
     * @param frameSize only as optimization, the number of bytes per sample
     *                  frame
     * @throws ArrayIndexOutOfBoundsException if one of the parameters is out of
     *                                        bounds
     */
    static void double2byte(List<double[]> input, int inOffset, byte[] output,
                            int outByteOffset, int frameCount, int formatCode, int channels,
                            int frameSize, double ditherBits) {
        int sampleSize = frameSize / channels;
        for (int channel = 0; channel < channels; channel++) {
            double[] data = (double[]) input.get(channel);
            double2byteGeneric(data, inOffset, output, outByteOffset, frameSize,
                    frameCount, formatCode, ditherBits);
            outByteOffset += sampleSize;
        }
    }
}
