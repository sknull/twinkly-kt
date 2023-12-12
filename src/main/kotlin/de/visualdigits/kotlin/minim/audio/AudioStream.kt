package de.visualdigits.kotlin.minim.audio

import de.visualdigits.kotlin.minim.buffer.MultiChannelBuffer


/**
 * An `AudioStream` is a stream of samples that is coming from
 * somewhere. Users of an `AudioStream` don't really need to know
 * where the samples are coming from. However, typically they will be read
 * from a `Line` or a file. An `AudioStream` needs to
 * be opened before being used and closed when you are finished with it.
 *
 * @author Damien Di Fede
 */
interface AudioStream : AudioResource {
    /**
     * Reads the next sample playable.
     *
     * @return an array of floats containing the value of each channel in the sample playable just read.
     * The size of the returned array will be the same size as getFormat().getChannels().
     */
    @Deprecated("")
    fun read(): DoubleArray

    /**
     * Reads buffer.getBufferSize() sample frames and puts them into buffer's channels.
     * The provided buffer will be forced to have the same number of channels that this
     * AudioStream does.
     *
     * @param buffer The MultiChannelBuffer to fill with audio samples.
     * @return int: the number of sample frames that were actually read, could be smaller than the size of the buffer.
     */
    fun read(buffer: MultiChannelBuffer): Int
}

