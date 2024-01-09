package de.visualdigits.kotlin.twinkly.model.playable.effects

import de.visualdigits.kotlin.XledArrayTest
import org.junit.jupiter.api.Test

class XledEffectTest : XledArrayTest() {

    @Test
    fun testWave() {
        val effect = WaveEffect(1.0, xledArray)
        effect.start()
        effect.join()
    }

    @Test
    fun testPlasma() {
        val effect = PlasmaEffect(10.0, 1.0, 1.0, xledArray)
        effect.start()
        effect.join()
    }

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
