package de.visualdigits.kotlin.twinkly.model.device.xled

import org.junit.jupiter.api.Test

class XLedDeviceTest {

    private val xled = XLedDevice("192.168.178.35", 10, 21)

    @Test
    fun testGetMode() {
        val mode = xled.getMode()
        println(mode)
    }
}
