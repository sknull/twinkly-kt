package de.visualdigits.kotlin.minim

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control


interface AudioResource {
    /**
     * Opens the resource to be used.
     */
    fun open()

    /**
     * Closes the resource, releasing any memory.
     */
    fun close()

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
}

