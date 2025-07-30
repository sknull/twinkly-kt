package de.visualdigits.kotlin.util

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TripleExtensionsTest {

    @Test
    fun testCompare() {
        val t1a = Triple(1, 1, 1)
        val t1b = Triple(1, 1, 1)
        val t2 = Triple(1, 2, 1)
        val t3 = Triple(1, 1, 2)
        val t4 = Triple(1, 2, 2)
        val t5 = Triple(2, 2, 2)
        val t6 = Triple(1, 2, 2)
        val t7 = Triple(2, 1, 2)
        val t8 = Triple(2, 2, 1)
        val t9 = Triple(2, 1, 1)

        assertTrue(t1a == t1b)

        assertTrue(t1a < t2)
        assertTrue(t1a < t3)
        assertTrue(t1a < t4)
        assertTrue(t1a < t5)
        assertTrue(t1a < t6)
        assertTrue(t1a < t7)
        assertTrue(t1a < t8)
        assertTrue(t1a < t9)

        assertTrue(t2 > t1a)
        assertTrue(t3 > t1a)
        assertTrue(t4 > t1a)
        assertTrue(t5 > t1a)
        assertTrue(t6 > t1a)
        assertTrue(t7 > t1a)
        assertTrue(t8 > t1a)
        assertTrue(t9 > t1a)
    }
}
