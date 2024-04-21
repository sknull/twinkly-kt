package de.visualdigits.kotlin.twinkly.model.playable.effects

import de.visualdigits.kotlin.XledArrayTest
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

class XledEffectTest : XledArrayTest() {

    @Test
    fun testElectricWave() {
        val maxR = max(xledArray.width, xledArray.height) / 2
        val sequence = XledSequence(frameDelay = 20)
        for (t in 0 until 360) {
            val frame = XledFrame(
                width = xledArray.width,
                height = xledArray.height
            )

            val cx = xledArray.width / 2
            val cy = xledArray.height / 2

            val a = 360.0 / maxR * 4
            for (r in 0 until maxR) {
                val v = (100 * (1 + sin(t + a * r * PI / 180.0)) / 2.0).roundToInt()
                frame.drawCircle(cx, cy, r, r, HSVColor(t, 100, v))
            }

            sequence.add(frame)
        }

        sequence.play(xledArray, loop = -1)
    }

    @Test
    fun testMatrix() {
        val effect = MatrixEffect(xledArray)
        effect.start()
        effect.join()
    }

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
