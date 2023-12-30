package de.visualdigits.kotlin.twinkly.model.device.xmusic

import org.junit.jupiter.api.Test

class XMusicTest {

    private val xmusic = XMusic("192.168.178.43")
    private val deviceInfo = xmusic.deviceInfo()

    @Test
    fun testMusicStats() {
        println(xmusic.musicMode())
        println(xmusic.musicMicEnabled())
        println(xmusic.musicConfig())
        while(true) {
            println(xmusic.musicStats())
            Thread.sleep(100)
        }
    }
}
