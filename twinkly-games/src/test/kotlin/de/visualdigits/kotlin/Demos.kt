package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.games.conway.Conway
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled("for local testing only")
class Demos : XledArrayTest() {

    @Test
    fun testConwaysGameOfLife() {
        val conway = Conway(
            preset = File(ClassLoader.getSystemResource("conway/conway_diehard.png").toURI()),
            xled = xledArray
        )
        conway.run()
    }
}
