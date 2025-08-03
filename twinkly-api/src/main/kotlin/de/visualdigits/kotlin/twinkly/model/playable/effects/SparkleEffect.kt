package de.visualdigits.kotlin.twinkly.model.playable.effects

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import kotlin.random.Random

class SparkleEffect(
    xled: XLedDevice,
    initialColor: Color<*> = RGBColor(0, 0, 0)
): XledEffect("Sparkle Effect", xled, initialColor = initialColor) {

    private val random = Random(System.currentTimeMillis())

    override fun getNextFrame() {
        setColor(initialColor)
        for (i in 0 until 100) {
            this[random.nextInt(0, width), random.nextInt(0, height)] = RGBColor(
                red = random.nextInt(0, 255),
                green = random.nextInt(0, 255),
                blue = random.nextInt(0, 255),
            )
        }
    }
}
