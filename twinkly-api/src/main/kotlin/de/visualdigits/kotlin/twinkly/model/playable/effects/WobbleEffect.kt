package de.visualdigits.kotlin.twinkly.model.playable.effects

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.HSVColor
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed

class WobbleEffect(
    xled: XLed,
    val duration: Long = 2000,
    val grid: Int = 20,
    initialColor: Color<*> = RGBColor(0, 0, 0)
): XledEffect("Wobble Effect", xled, frameDelay = duration / grid, initialColor = initialColor) {

    private var time: Int = 0
    private var timeStep : Int = 1
    var color: HSVColor = HSVColor(0, 100, 100)
    private var colorStep: Int = 3

    override fun getNextFrame() {
        for (x in 0 until width / grid * grid step grid) {
            for (y in 0 until height / grid * grid step grid) {
                for (yy in 0 until grid - 1 - time) {
                    this[x, y + yy] = color
                    this[x + 1, y + yy] = color
                    this[x + grid - 2, y + yy] = color
                    this[x + grid - 3, y + yy] = color
                }
                for (yy in grid - 1 - time until grid - 1) {
                    this[x, y + yy] = initialColor
                    this[x + 1, y + yy] = initialColor
                    this[x + grid - 2, y + yy] = initialColor
                    this[x + grid - 3, y + yy] = initialColor
                }
                for (xx in 2 until grid - 3) {
                    for (yy in 0 until time) {
                        this[x + xx, y + yy] = color
                    }
                }
                for (xx in 2 until grid - 3) {
                    for (yy in time until grid - 1) {
                        this[x + xx, y + yy] = initialColor
                    }
                }
            }
        }
        time += timeStep
        if (time == 0 || time == grid - 1) {
            timeStep *= -1
        }
        color.h += colorStep
        if (color.h < 0 || color.h > 360) {
            colorStep *= -1
        }
    }

}
