package de.visualdigits.kotlin.util

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("only for local testing")
class SystemUtilsTest {

    @Test
    fun testUserHome() {
        val userHome = SystemUtils.getUserHome()
        println(userHome)
    }
}
