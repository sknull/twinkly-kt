package de.visualdigits.kotlin.minim.buffer

import kotlin.math.sqrt

/**
 * MultiChannelBuffer represents a chunk of multichannel (or mono) audio data.
 * It is primarily used internally when passing buffers of audio around, but
 * you will need to create one to use things like the loadFileIntoBuffer method of Minim
 * and the setSample method of Sampler. When thinking about a buffer of audio
 * we usually consider how many *sample frames* long that buffer is. This
 * is not the same as the actual number of values stored in the buffer. Mono, or
 * single channel audio, contains one sample per sample playable, but stereo is
 * two, quadraphonic is four, and so forth. The buffer size of a MultiChannelBuffer
 * is how many sample frames it stores, so when retrieving and setting values
 * it is required to indicate which channel should be operated upon.
 *
 * @author Damien Di Fede
 *  *  */
class MultiChannelBuffer(var bufferSize: Int, numChannels: Int) {
    // TODO: consider just wrapping a FloatSampleBuffer
    private var channels: Array<DoubleArray>

    /**
     * Construct a MultiChannelBuffer, providing a size and number of channels.
     *
     */
    init {
        channels = Array(numChannels) { DoubleArray(bufferSize) { 0.0 } }
    }

    /**
     * Copy the data in the provided MultiChannelBuffer to this MultiChannelBuffer.
     * Doing so will change both the buffer size and channel count of this
     * MultiChannelBuffer to be the same as the copied buffer.
     *
     * @param otherBuffer the MultiChannelBuffer to copy
     * @shortdesc Copy the data in the provided MultiChannelBuffer to this MultiChannelBuffer.
     */
    fun set(otherBuffer: MultiChannelBuffer) {
        bufferSize = otherBuffer.bufferSize
        channels = otherBuffer.channels.clone()
    }

    fun getChannelCount(): Int = channels.size

    /**
     * Set the number of channels this buffer contains.
     * Doing this will retain any existing channels
     * under the new channel count.
     *
     * @param numChannels int: the number of channels this buffer should contain
     * @shortdesc Set the number of channels this buffer contains.
     */
    fun setChannelCount(numChannels: Int) {
        if (channels.size != numChannels) {
            val newChannels = Array(numChannels) {DoubleArray(bufferSize) { 0.0 } }
            var c = 0
            while (c < channels.size && c < numChannels) {
                newChannels[c] = channels[c]
                ++c
            }
            channels = newChannels
        }
    }

    /**
     * Returns the value of a sample in the given channel,
     * at the given offset from the beginning of the buffer.
     * When sampleIndex is a float, this returns an interpolated
     * sample value. For instance, getSample( 0, 30.5f ) will
     * return an interpolated sample value in channel 0 that is
     * between the value at 30 and the value at 31.
     *
     * @param channelNumber int: the channel to get the sample value from
     * @param sampleIndex   int: the offset from the beginning of the buffer, in samples.
     * @return float: the value of the sample
     * @shortdesc Returns the value of a sample in the given channel,
     * at the given offset from the beginning of the buffer.
     */
    fun getSample(channelNumber: Int, sampleIndex: Int): Double {
        return channels[channelNumber][sampleIndex]
    }

    /**
     * Returns the interpolated value of a sample in the given channel,
     * at the given offset from the beginning of the buffer,
     * For instance, getSample( 0, 30.5f ) will
     * return an interpolated sample value in channel 0 that is
     * between the value at 30 and the value at 31.
     *
     * @param channelNumber int: the channel to get the sample value from
     * @param sampleIndex   float: the offset from the beginning of the buffer, in samples.
     * @return float: the value of the sample
     */
    fun getSample(channelNumber: Int, sampleIndex: Float): Double {
        val lowSamp = sampleIndex.toInt()
        val hiSamp = lowSamp + 1
        if (hiSamp == bufferSize) {
            return channels[channelNumber][lowSamp]
        }
        val lerp = sampleIndex - lowSamp
        return channels[channelNumber][lowSamp] + lerp * (channels[channelNumber][hiSamp] - channels[channelNumber][lowSamp])
    }

    /**
     * Sets the value of a sample in the given channel at the given
     * offset from the beginning of the buffer.
     *
     * @param channelNumber int: the channel of the buffer
     * @param sampleIndex   int: the sample offset from the beginning of the buffer
     * @param value         float: the sample value to set
     */
    fun setSample(channelNumber: Int, sampleIndex: Int, value: Double) {
        channels[channelNumber][sampleIndex] = value
    }

    /**
     * Calculates the RMS amplitude of one of the buffer's channels.
     *
     * @param channelNumber int: the channel to use
     * @return float: the RMS amplitude of the channel
     *      */
    fun getLevel(channelNumber: Int): Double {
        val samples = channels[channelNumber]
        var level = 0.0
        for (i in samples.indices) {
            val value = samples[i]
            level += value * value
        }
        level /= samples.size
        level = sqrt(level)
        return level
    }

    /**
     * Returns the requested channel as a float array.
     * You should not necessarily assume that the
     * modifying the returned array will modify
     * the values in this buffer.
     *
     * @param channelNumber int: the channel to return
     * @return float[]: the channel represented as a float array
     * @shortdesc Returns the requested channel as a float array.
     */
    fun getChannel(channelNumber: Int): DoubleArray {
        return channels[channelNumber]
    }

    /**
     * Sets all of the values in a particular channel using
     * the values of the provided float array. The array
     * should be at least as long as the current buffer size
     * of this buffer and this will only copy as many samples
     * as fit into its current buffer size.
     *
     * @param channelNumber int: the channel to set
     * @param samples       float[]: the array of values to copy into the channel
     * @shortdesc Sets all of the values in a particular channel using
     * the values of the provided float array.
     */
    fun setChannel(channelNumber: Int, samples: DoubleArray) {
        System.arraycopy(samples, 0, channels[channelNumber], 0, bufferSize)
    }

    /**
     * Set the length of this buffer in sample frames.
     * Doing this will retain all of the sample data
     * that can fit into the new buffer size.
     *
     * @param bufferSize int: the new length of this buffer in sample frames
     * @shortdesc Set the length of this buffer in sample frames.
     */
    fun setBufferSizeInFrames(bufferSize: Int) {
        if (this.bufferSize != bufferSize) {
            this.bufferSize = bufferSize
            for (i in channels.indices) {
                val newChannel = DoubleArray(bufferSize) { 0.0 }
                // copy existing data into the new channel array
                System.arraycopy(
                    channels[i], 0, newChannel, 0,
                    if (bufferSize < channels[i].size) bufferSize else channels[i].size
                )
                channels[i] = newChannel
            }
        }
    }
}

