package de.visualdigits.kotlin.twinkly.model.playable.effects

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledRepeater
import java.lang.Thread.sleep

abstract class XledEffect(
    val name: String,
    val xled: XLed,
    frameDelay: Long = 1000 / 12,
    initialColor: Color<*> = RGBColor(0, 0, 0)
): XledFrame(xled.width, xled.height, frameDelay = frameDelay, initialColor = initialColor) {

    private val repeater = XledRepeater(name, xled, this, frameDelay)

    open fun reset(numFrames: Int? = null) {
        // nothing to do here
    }

    abstract fun getNextFrame()


    fun start() {
        xled.setMode(DeviceMode.rt)
        reset()
        while (true) {
            xled.showRealTimeFrame(this)
            getNextFrame()
            sleep(frameDelay)
        }
//        repeater.start()
    }

    fun play() {
//        repeater.play()
    }

    fun pause() {
//        repeater.pause()
    }

    fun end() {
//        repeater.end()
    }

    fun join() {
//        repeater.join()
    }
}
