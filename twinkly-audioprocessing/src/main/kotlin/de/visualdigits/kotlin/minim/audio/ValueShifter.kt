package de.visualdigits.kotlin.minim.audio

// a small class to interpolate a value over time
class ValueShifter(private val vstart: Float, private val vend: Float, t: Int) {

    private val tstart: Float
    private val tend: Float

    init {
        tstart = System.currentTimeMillis().toInt().toFloat()
        tend = tstart + t
    }

    fun value(): Float {
        val millis = System.currentTimeMillis().toInt()
        val norm = (millis - tstart) / (tend - tstart)
        val range = vend - vstart
        return vstart + range * norm
    }

    fun done(): Boolean {
        return System.currentTimeMillis().toInt() > tend
    }
}
