package de.visualdigits.kotlin.util

import org.junit.jupiter.api.Test

class SystemUtilsTest {

    @Test
    fun testUserHome() {
        val userHome = SystemUtils.getUserHome()
        println(userHome)
    }
}
