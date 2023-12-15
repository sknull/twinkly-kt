package de.visualdigits.kotlin.minim.audio

import de.visualdigits.kotlin.minim.buffer.MultiChannelBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control


interface AudioResource : AutoCloseable {
    /**
     * Opens the resource to be used.
     */
    fun open()

    /**
     * Returns the Controls available for this AudioResource.
     *
     * @return an array of Control objects, that can be used to manipulate the
     * resource
     */
    fun getControls(): Array<Control>

    /**
     * Returns the AudioFormat of this AudioResource.
     *
     * @return the AudioFormat of this AudioResource
     */
    fun getFormat(): AudioFormat

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

