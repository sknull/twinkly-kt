package de.visualdigits.kotlin.minim.buffer

import de.visualdigits.kotlin.minim.audio.AudioListener
import de.visualdigits.kotlin.minim.audio.Controller

class StereoBuffer(channels: Int, bufferSize: Int, c: Controller) : AudioListener {

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

    override fun samples(sampL: FloatArray, sampR: FloatArray?) {
        left.set(sampL)
        if (sampR != null) {
            right.set(sampR)
            mix.mix(sampL, sampR)
        }
        parent.update()
    }
}

