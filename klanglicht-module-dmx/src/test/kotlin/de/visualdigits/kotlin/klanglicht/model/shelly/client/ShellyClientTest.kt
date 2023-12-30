package de.visualdigits.kotlin.klanglicht.model.shelly.client

import org.junit.jupiter.api.Test

class ShellyClientTest {

    @Test
    fun testPower() {
        ShellyClient.setPower(ipAddress = "192.168.178.38", command = "relay/0", turnOn = false)
    }
}
