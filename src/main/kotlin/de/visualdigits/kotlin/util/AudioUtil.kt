package de.visualdigits.kotlin.util

import ddf.minim.Minim
import ddf.minim.analysis.FFT
import java.io.File

class AudioUtil {

    fun analyze() {
        val minim = Minim(this)
        val player = minim.loadFile("M:\\Electronic\\Kraftwerk\\Der Katalog\\2003_Tour De France\\07_Aero Dynamik.mp3")
        player.play()
    }
}
