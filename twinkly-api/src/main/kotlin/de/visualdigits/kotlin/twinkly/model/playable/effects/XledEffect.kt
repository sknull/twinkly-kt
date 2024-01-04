package de.visualdigits.kotlin.twinkly.model.playable.effects

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledRepeater

abstract class XledEffect(
    val name: String,
    val xled: XLed,
    frameDelay: Long = 1000 / 12,
    initialColor: Color<*> = RGBColor(0, 0, 0)
): XledFrame(xled.width, xled.height, frameDelay = frameDelay, initialColor = initialColor) {

    private val repeater = XledRepeater(name, xled, this, frameDelay)

    abstract fun reset(numFrames: Int? = null)

    abstract fun getNextFrame()


    fun start() {
        xled.setMode(DeviceMode.rt)
        reset()
        repeater.start()
    }

    fun play() {
        repeater.play()
    }

    fun pause() {
        repeater.pause()
    }

    fun end() {
        repeater.end()
    }

    fun join() {
        repeater.join()
    }
}
