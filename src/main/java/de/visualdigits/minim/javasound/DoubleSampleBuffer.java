/*
 * FloatSampleBuffer.java
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
import java.util.ArrayList;
import java.util.List;

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
 * @author Florian Bomers
 */

public class DoubleSampleBuffer {

    /**
     * Constant for setDitherMode: dithering will be enabled if sample size is
     * decreased
     */
    public static final int DITHER_MODE_AUTOMATIC = 0;
    /**
     * Constant for setDitherMode: dithering will be done
     */
    public static final int DITHER_MODE_ON = 1;
    /**
     * Constant for setDitherMode: dithering will not be done
     */
    public static final int DITHER_MODE_OFF = 2;

    public static final int INITIAL_CHANNELS = 2;
    public static final double DEFAULT_DITHER_BITS = 0.7f;

    // one double array for each channel
    private final List<double[]> channels = new ArrayList<>();
    private int sampleCount = 0;
    private int channelCount = 0;
    private double sampleRate = 0;
    private int originalFormatType = 0;
    // cache for performance
    private AudioFormat lastConvertToByteArrayFormat = null;
    private int lastConvertToByteArrayFormatCode = 0;

    /**
     * Create an empty FloatSampleBuffer with the specified number of channels,
     * samples, and the specified sample rate.
     */
    public DoubleSampleBuffer(int channelCount, int sampleCount, double sampleRate) {
        init(channelCount, sampleCount, sampleRate);
        System.out.println("");
    }

    /**
     * Initialize this sample buffer to have the specified channels, sample
     * count, and sample rate. If lazy is true, as much as possible will
     * existing arrays be reused. Otherwise, any hidden channels are freed.
     *
     * @param newChannelCount
     * @param newSampleCount
     * @param newSampleRate
     * @throws IllegalArgumentException if newChannelCount or newSampleCount are
     *                                  negative, or newSampleRate is not positive.
     */
    public void init(int newChannelCount, int newSampleCount, double newSampleRate) {
        if (newChannelCount < 0 || newSampleCount < 0 || newSampleRate <= 0.0f) {
            throw new IllegalArgumentException(
                    "invalid parameters in initialization of FloatSampleBuffer.");
        }
        grow(INITIAL_CHANNELS);
        setSampleRate(newSampleRate);
        if (this.sampleCount != newSampleCount
                || this.channelCount != newChannelCount) {
            createChannels(newChannelCount, newSampleCount);
        }
    }
    /**
     * Grow the channels array to allow at least channelCount elements. If
     * !lazy, then channels will be resized to be exactly channelCount elements.
     * The new elements will be null.
     *
     * @param newChannelCount
     */
    private void grow(int newChannelCount) {
        if (channels.size() < newChannelCount) {
            for (int i = 0; i < newChannelCount - channels.size(); i++) {
                channels.add(null);
            }
        }
    }

    private void createChannels(int newChannelCount, int newSampleCount) {
        setSampleCountImpl(newSampleCount);
        // grow the array, if necessary. Intentionally lazy here!
        grow(newChannelCount);
        // lazy delete of all channels. Intentionally lazy !
        setChannelCountImpl(0);
        for (int ch = 0; ch < newChannelCount; ch++) {
            insertChannel(ch, false);
        }
    }

    /**
     * Inserts a channel at position <code>index</code>.
     * <p>
     * If <code>silent</code> is true, the new channel will be silent.
     * Otherwise it will contain random data.
     * <p>
     * If <code>lazy</code> is true, hidden channels which have at least
     * sampleCount elements will be examined for reusage as inserted
     * channel.<br>
     * If <code>lazy</code> is false, still hidden channels are reused, but it
     * is assured that the inserted channel has exactly sampleCount
     * elements, thus not wasting memory.
     */
    public void insertChannel(int index, boolean silent) {
        grow(this.channelCount + 1);
        int physSize = channels.size();
        int virtSize = this.channelCount;
        double[] newChannel = null;
        if (physSize > virtSize) {
            // there are hidden channels. Try to use one.
            for (int ch = virtSize; ch < physSize; ch++) {
                double[] thisChannel = channels.get(ch);
                if (thisChannel != null) {
                    // we found a matching channel. Use it !
                    newChannel = thisChannel;
                    channels.set(ch,  null);
                    break;
                }
            }
        }
        if (newChannel == null) {
            newChannel = new double[sampleCount];
        }
        // move channels after index
        for (int i = index; i < virtSize; i++) {
            channels.set(i + 1, channels.get(i));
        }
        channels.set(index, newChannel);
        setChannelCountImpl(this.channelCount + 1);
        if (silent) {
            makeSilence(index);
        }
        // if not lazy, remove old channels
        grow(this.channelCount);
    }

    /**
     * Silence the specified channel
     */
    public void makeSilence(int channel) {
        makeSilence(channel, 0, sampleCount);
    }

    /**
     * Silence the specified channel in the specified range
     */
    public void makeSilence(int channel, int offset, int count) {
        if (offset < 0 || (count + offset) > sampleCount || count < 0) {
            throw new IllegalArgumentException(
                    "offset and/or sampleCount out of bounds");
        }
        makeSilence(getChannel(channel), offset, count);
    }

    private void makeSilence(double[] samples, int offset, int count) {
        count += offset;
        for (int i = offset; i < count; i++) {
            samples[i] = 0.0f;
        }
    }

    /**
     * Get the actual audio data of one channel.<br>
     * Modifying this array will modify the audio samples of this
     * FloatSampleBuffer. <br>
     * NOTE: the returned array may be larger than sampleCount. So in any case,
     * sampleCount is to be respected.
     *
     * @throws IllegalArgumentException if channel is out of bounds
     */
    public double[] getChannel(int channel) {
        if (channel >= this.channelCount) {
            throw new IllegalArgumentException(
                    "FloatSampleBuffer: invalid channel number.");
        }
        return (double[]) channels.get(channel);
    }

    /**
     * internal setter for channel count, just change the variable. From
     * outside, use addChannel, insertChannel, removeChannel
     */
    protected void setChannelCountImpl(int newChannelCount) {
        if (channelCount != newChannelCount) {
            channelCount = newChannelCount;
            // remove cache
            this.lastConvertToByteArrayFormat = null;
        }
    }

    
    /**
     * internal setter for sample count, just change the variable. From outside,
     * use changeSampleCount
     */
    protected void setSampleCountImpl(int newSampleCount) {
        if (sampleCount != newSampleCount) {
            sampleCount = newSampleCount;
        }
    }

    /**
     * Sets the sample rate of this buffer. NOTE: no conversion is done. The
     * samples are only re-interpreted.
     */
    public void setSampleRate(double sampleRate) {
        if (sampleRate <= 0) {
            throw new IllegalArgumentException(
                    "Invalid samplerate for FloatSampleBuffer.");
        }
        if (this.sampleRate != sampleRate) {
            this.sampleRate = sampleRate;
            // remove cache
            lastConvertToByteArrayFormat = null;
        }
    }

    /**
     * @return the required size of the buffer for calling
     * convertToByteArray(..) is called
     */
    public int getByteArrayBufferSize(AudioFormat format) {
        return getByteArrayBufferSize(format, sampleCount);
    }

    /**
     * @param lenInSamples how many samples to be considered
     * @return the required size of the buffer for the given number of samples
     * for calling convertToByteArray(..)
     */
    public int getByteArrayBufferSize(AudioFormat format, int lenInSamples) {
        // make sure this format is supported
        checkFormatSupported(format);
        return format.getFrameSize() * lenInSamples;
    }

    /**
     * Verify that the specified AudioFormat can be converted to and from. If
     * the format is not supported, an IllegalArgumentException is thrown.
     *
     * @throws IllegalArgumentException if the format is not supported
     */
    public static void checkFormatSupported(AudioFormat format) {
        DoubleSampleTools.getFormatType(format);
    }

    /**
     * Writes this sample buffer's audio data to <code>buffer</code> as an
     * interleaved byte array. <code>buffer</code> must be large enough to
     * hold all data.
     *
     * @return number of bytes written to <code>buffer</code>
     * @throws IllegalArgumentException when buffer is too small or
     *                                  <code>format</code> doesn't match
     */
    public int convertToByteArray(byte[] buffer, int offset, AudioFormat format) {
        return convertToByteArray(0, sampleCount, buffer, offset, format);
    }

    /**
     * Writes this sample buffer's audio data to <code>buffer</code> as an
     * interleaved byte array. <code>buffer</code> must be large enough to
     * hold all data.
     *
     * @param readOffset   the sample offset from where samples are read from this
     *                     FloatSampleBuffer
     * @param lenInSamples how many samples are converted
     * @param buffer       the byte buffer written to
     * @param writeOffset  the byte offset in buffer
     * @return number of bytes written to <code>buffer</code>
     * @throws IllegalArgumentException when buffer is too small or
     *                                  <code>format</code> doesn't match
     */
    public int convertToByteArray(int readOffset, int lenInSamples,
                                  byte[] buffer, int writeOffset, AudioFormat format) {
        int byteCount = format.getFrameSize() * lenInSamples;
        if (writeOffset + byteCount > buffer.length) {
            throw new IllegalArgumentException(
                    "FloatSampleBuffer.convertToByteArray: buffer too small.");
        }
        if (format != lastConvertToByteArrayFormat) {
            if (format.getSampleRate() != sampleRate) {
                throw new IllegalArgumentException(
                        "FloatSampleBuffer.convertToByteArray: different samplerates.");
            }
            if (format.getChannels() != channelCount) {
                throw new IllegalArgumentException(
                        "FloatSampleBuffer.convertToByteArray: different channel count.");
            }
            lastConvertToByteArrayFormat = format;
            lastConvertToByteArrayFormatCode = DoubleSampleTools.getFormatType(format);
        }
        DoubleSampleTools.double2byte(channels, readOffset, buffer, writeOffset,
                lenInSamples, lastConvertToByteArrayFormatCode,
                format.getChannels(), format.getFrameSize(),
                getConvertDitherBits(lastConvertToByteArrayFormatCode));

        return byteCount;
    }

    /**
     * @return the ditherBits parameter for the double2byte functions
     */
    protected double getConvertDitherBits(int newFormatType) {
        // let's see whether dithering is necessary
        boolean doDither = false;
        switch (newFormatType) {
            case DITHER_MODE_AUTOMATIC:
                doDither = (originalFormatType & DoubleSampleTools.F_SAMPLE_WIDTH_MASK) > (0);
                break;
            case DITHER_MODE_ON:
                doDither = true;
                break;
            case DITHER_MODE_OFF:
                break;
        }
        return doDither ? DEFAULT_DITHER_BITS : 0.0f;
    }

    /**
     * Silence the entire audio buffer.
     */
    public void makeSilence() {
        makeSilence(0, sampleCount);
    }

    /**
     * Silence the entire buffer in the specified range on all channels.
     */
    public void makeSilence(int offset, int count) {
        if (offset < 0 || (count + offset) > sampleCount || count < 0) {
            throw new IllegalArgumentException(
                    "offset and/or sampleCount out of bounds");
        }
        // silence all channels
        int localChannelCount = channelCount;
        for (int ch = 0; ch < localChannelCount; ch++) {
            makeSilence(getChannel(ch), offset, count);
        }
    }

    /**
     * Initializes audio data from the provided byte array. The double samples
     * are written at <code>destOffset</code>. This FloatSampleBuffer must be
     * big enough to accomodate the samples.
     * <p>
     * <code>srcBuffer</code> is read from index <code>srcOffset</code> to
     * <code>(srcOffset + (lengthInSamples * format.getFrameSize()))</code>.
     *
     * @param input        the input buffer in interleaved audio data
     * @param inByteOffset the offset in <code>input</code>
     * @param format       input buffer's audio format
     * @param doubleOffset  the offset where to write the double samples
     * @param frameCount   number of samples to write to this sample buffer
     */
    public void setSamplesFromBytes(byte[] input, int inByteOffset,
                                    AudioFormat format, int doubleOffset, int frameCount) {
        if (doubleOffset < 0 || frameCount < 0 || inByteOffset < 0) {
            throw new IllegalArgumentException(
                    "FloatSampleBuffer.setSamplesFromBytes: negative inByteOffset, doubleOffset, or frameCount");
        }
        if (inByteOffset + (frameCount * format.getFrameSize()) > input.length) {
            throw new IllegalArgumentException(
                    "FloatSampleBuffer.setSamplesFromBytes: input buffer too small.");
        }
        if (doubleOffset + frameCount > sampleCount) {
            throw new IllegalArgumentException(
                    "FloatSampleBuffer.setSamplesFromBytes: frameCount too large");
        }
        DoubleSampleTools.byte2double(input, inByteOffset, channels, doubleOffset,
                frameCount, format, false);
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public double getSampleRate() {
        return sampleRate;
    }
}
