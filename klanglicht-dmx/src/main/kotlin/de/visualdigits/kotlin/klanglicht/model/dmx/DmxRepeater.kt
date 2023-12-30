package de.visualdigits.kotlin.klanglicht.model.dmx

import org.slf4j.LoggerFactory

class DmxRepeater private constructor(
    val dmxInterface: DmxInterface
) : Thread("DMX Repeater") {

    private var log = LoggerFactory.getLogger(DmxRepeater::class.java)

    private var loop = false

    private var running = false

    companion object {

        var dmxRepeater: DmxRepeater? = null

        fun instance(
            dmxInterface: DmxInterface,
        ): DmxRepeater {
            if (dmxRepeater == null) {
                dmxRepeater = DmxRepeater(dmxInterface)
                dmxRepeater?.start()
            }
            return dmxRepeater!!
        }
    }

    override fun run() {
        running = true
        loop = true
        log.info("### repeater started")
        while (loop) {
            if (running) {
                dmxInterface.write()
                sleep(1000)
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
