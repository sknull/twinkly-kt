package de.visualdigits.kotlin.twinkly.model.playable.effects

import de.visualdigits.kotlin.XledArrayTest
import org.junit.jupiter.api.Test

class XledEffectTest : XledArrayTest() {

    @Test
    fun testSparkle() {
        val effect = SparkleEffect(xledArray)
        effect.start()
        effect.join()
    }

    @Test
    fun testWobble() {
        val effect = WobbleEffect(xledArray)
        effect.start()
        effect.join()
    }
}
