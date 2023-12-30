package de.visualdigits.kotlin.klanglicht.rest.dmx.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DmxControllerTest {

    @Test
    fun testSplit() {
        val baseChannels = ",,1,21,15,29,,,"
        val lIds = baseChannels
            .split(",")
            .filter { it.isNotEmpty() }
            .map { it.toInt() }
        assertEquals(listOf(1,21,15,29), lIds)
    }
}
