package de.visualdigits.kotlin.twinkly.model.device

import org.junit.jupiter.api.Test

class TwinklyDiscoveryKtTest {

    @Test
    fun testDiscoverDevices() {
        val devices = Session.discoverTwinklyDevices()
        println(devices)
    }

}
