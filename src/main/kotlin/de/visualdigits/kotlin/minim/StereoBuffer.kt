package de.visualdigits.kotlin.minim

import org.slf4j.LoggerFactory

class StereoBuffer(channels: Int, bufferSize: Int, c: Controller) : AudioListener {

    private val log = LoggerFactory.getLogger(StereoBuffer::class.java)

    var left: MAudioBuffer
    var right: MAudioBuffer
    var mix: MAudioBuffer

    private val parent: Controller

    init {
        left = MAudioBuffer(bufferSize)
        if (channels == 1) {
            right = left
            mix = left
        }
        else {
            right = MAudioBuffer(bufferSize)
            mix = MAudioBuffer(bufferSize)
        }
        parent = c
    }

    override fun samples(samp: DoubleArray) {
        // log.debug("Got samples!");
        left.set(samp)
        parent.update()
    }

    override fun samples(sampL: DoubleArray, sampR: DoubleArray) {
        left.set(sampL)
        right.set(sampR)
        mix.mix(sampL, sampR)
        parent.update()
    }
}

