package de.visualdigits.kotlin.twinkly.model.playable.effects

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

class WaveEffect(
    val intensity: Double,
    xled: XLed,
    initialColor: Color<*> = RGBColor(0, 0, 0)
): XledEffect("Plasma Effect", xled, initialColor = initialColor) {

    private val intervalStep = 2.0 * PI / 4.0

    private val maxShift = 40.0 / 360.0

    private var time = 0.0

    private val step = 1.0 / 360.0

    override fun reset(numFrames: Int?) {
    }

    override fun getNextFrame() {
        val color = HSVColor(0, 100, 100)
        for (x in 0 until xled.width) {
            for (y in 0 until xled.height) {
                var aux = intensity * (time * (2 * PI) + (intervalStep * y));
                var ysin = sin((aux * 4.0) % (2.0 * PI));
                var h = color.h / 360.0 + sin(aux % (2.0 * PI)) * maxShift;
                if (h < 0.0)
                    h = h + 1.0;
                if (h > 1.0)
                    h = h - 1.0;
                color.v = 100//(100.0 * (ysin + 1.0) / 2.0).roundToInt();
                this[x, y] = HSVColor((360.0 * h).roundToInt(), color.s, color.v)

                time += step
                if (time > 1.0) {
                    time = 0.0
                }
            }
        }
    }
}
