package de.visualdigits.kotlin.twinkly.model.device.xled

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("only for local testing")
class XLedDeviceTest {

    private val xled = XLedDevice.instance(ipAddress = "192.168.178.35", width = 10, height = 21)

    @Test
    fun testGetMode() {
        val mode = xled.getMode()
        println(mode)
    }
}
