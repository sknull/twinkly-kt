package de.visualdigits.kotlin.minim


enum class AudioInputType(val channels: Int) {
    /**
     * Specifies that you want a MONO AudioInput or AudioOutput
     */
    MONO(1),

    /**
     * Specifies that you want a STEREO AudioInput or AudioOutput
     */
    STEREO(2)
}

