package de.visualdigits.kotlin.twinkly.model.device

import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import org.junit.jupiter.api.Test

class TwinklyDiscoveryKtTest {

    @Test
    fun testDiscoverDevices() {
        val devices = XLedDevice.discoverTwinklyDevices()
        println(devices)
    }

}
