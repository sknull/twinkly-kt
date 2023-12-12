package de.visualdigits.kotlin.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class CoroutineTest {

    private var running: Boolean = false

    @Test
    fun testCoroutines() {
        runBlocking {
            running = true
            parallel()
        }
    }

    suspend fun parallel() {
        coroutineScope {
            awaitAll(
                async { task1() },
                async { task2() },
                async { stopper() }
            )
            println("finished")
        }
    }

    suspend fun task1() {
        while (running) {
            println("task 1")
            delay(1000)
        }
    }

    suspend fun task2() {
        while (running) {
            println("task 2")
            delay(1300)
        }
    }

    suspend fun stopper() {
        delay(20000)
        running = false
    }
}
