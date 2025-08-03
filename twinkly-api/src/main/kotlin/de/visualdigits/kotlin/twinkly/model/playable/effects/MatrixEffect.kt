package de.visualdigits.kotlin.twinkly.model.playable.effects

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class MatrixEffect(
    xled: XLedDevice,
    val color: RGBColor = RGBColor(0, 255, 0),
    val interval: Int = 3,
    initialColor: Color<*> = RGBColor(0, 0, 0)
): XledEffect("Matrix Effect", xled, initialColor = initialColor) {

    private val hsvColor = color.toHsvColor()

    private var chars: MutableList<Pair<XledFrame, Pair<Int, Int>>> = mutableListOf()

    private val random = Random(System.currentTimeMillis())

    private var i = interval

    override fun reset(numFrames: Int?) {
        for (n in 0 until 100) {
            val frame = XledFrame(width = 3, height = 3)
            for (y in 0 until 3) {
                for (x in 0 until 3) {
                    hsvColor.v = random.nextInt(0, 100)
                    frame[x, y] = hsvColor.toRgbColor()
                }
            }
            val px = random.nextInt(0, width / 3) * 3
            val py = random.nextInt(0, height / 3) * 3
            chars.add(Pair(frame, Pair(px, py)))
        }
    }

    override fun getNextFrame() {
        if (i-- == 0) {
            i = interval

            val frame = XledFrame(width = 3, height = 3)
            for (y in 0 until 3) {
                for (x in 0 until 3) {
                    hsvColor.v = random.nextInt(0, 100)
                    frame[x, y] = hsvColor.toRgbColor()
                }
            }
            val px = random.nextInt(0, width / 3) * 3
            val py = random.nextInt(0, height / 3) * 3
            chars.add(Pair(frame, Pair(px, py)))
            replaceSubFrame(frame, px, py)
        }

        chars = chars.map { entry ->
            val ppx = entry.second.first
            val ppy = entry.second.second
            val frame = entry.first
            for (n in 0 .. ppy step 3) {
                val f = frame.clone()
                for (y in 0 until 3) {
                    for (x in 0 until 3) {
                        val hsv = frame[x, y].toHsvColor()
                        hsv.v = max(hsv.v - 3 * n, 0)
                        f[x, y] = hsv.toRgbColor()
                    }
                }
                replaceSubFrame(f, ppx, ppy - n)
            }
            Pair(frame, Pair(ppx, min(ppy + 3, height - 1)))
        }.toMutableList()
    }
}
