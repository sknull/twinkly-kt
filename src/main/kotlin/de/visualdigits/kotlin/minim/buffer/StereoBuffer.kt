package de.visualdigits.kotlin.minim.buffer

import de.visualdigits.kotlin.minim.audio.AudioInput
import de.visualdigits.kotlin.minim.audio.AudioListener

class StereoBuffer(
    channels: Int,
    bufferSize: Int,
    c: AudioInput
) : AudioListener {

    var left: AudioBuffer
    var right: AudioBuffer
    var mix: AudioBuffer

    private val parent: AudioInput

    init {
        left = AudioBuffer(bufferSize)
        if (channels == 1) {
            right = left
            mix = left
        }
        else {
            right = AudioBuffer(bufferSize)
            mix = AudioBuffer(bufferSize)
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

