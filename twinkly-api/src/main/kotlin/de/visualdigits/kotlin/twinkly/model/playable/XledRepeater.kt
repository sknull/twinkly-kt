package de.visualdigits.kotlin.twinkly.model.playable

import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.playable.effects.XledEffect
import org.slf4j.LoggerFactory

class XledRepeater(
    val effectName: String,
    val xled: XLedDevice,
    val effect: XledEffect,
    val frameDelay: Long = 1000 / 12
) : Thread("Xled Repeater - $effectName") {

    private var log = LoggerFactory.getLogger(XledRepeater::class.java)

    private var loop = false

    private var running = false

    override fun run() {
        running = true
        loop = true
        log.info("### repeater started")
        while (loop) {
            if (running) {
                xled.showRealTimeFrame(effect)
                effect.getNextFrame()
                sleep(frameDelay)
            }
        }
    }

    fun play() {
        running = true
    }

    fun pause() {
        running = false
    }

    fun end() {
        loop = false
    }
}
