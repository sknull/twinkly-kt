package de.visualdigits.kotlin.twinkly.model.device.xmusic

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("only for local testing")
class XMusicTest {

    private val xmusic = XMusic("192.168.178.43")
    private val deviceInfo = xmusic.deviceInfo

    @Test
    fun testMusicStats() {
        println(xmusic.getMusicMode())
        println(xmusic.isMusicMicEnabled())
        println(xmusic.getMusicConfig())
        while(true) {
            println(xmusic.getMusicStats())
            Thread.sleep(100)
        }
    }
}
