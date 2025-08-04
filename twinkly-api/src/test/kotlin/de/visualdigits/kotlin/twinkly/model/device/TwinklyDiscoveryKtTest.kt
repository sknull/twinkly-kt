package de.visualdigits.kotlin.twinkly.model.device

import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import org.junit.jupiter.api.Test

class TwinklyDiscoveryKtTest {

    @Test
    fun testDiscoverDevices() {
        val devices = XLed.discoverTwinklyDevices()
        println(devices)
    }

}
