package de.visualdigits.kotlin.twinkly.model.playable

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionDirection
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionType

interface Playable {

    var running: Boolean

    fun playAsync(
        xled: XLed,
        loop: Int = -1,
        random: Boolean = false,
        transitionType: TransitionType = TransitionType.STRAIGHT,
        transitionDirection: TransitionDirection = TransitionDirection.LEFT_RIGHT,
        transitionBlendMode: BlendMode = BlendMode.REPLACE,
        transitionDuration: Long = 2550,
        verbose: Boolean = false
    ) {
        val runner = LoopRunner(
            playable = this,
            xled = xled,
            loop = loop,
            random = random,
            transitionType = transitionType,
            transitionDirection = transitionDirection,
            transitionBlendMode = transitionBlendMode,
            transitionDuration = transitionDuration,
            verbose = verbose
        )
        runner.start()
    }

    private class LoopRunner(
        private val playable: Playable,
        private val xled: XLed,
        private val loop: Int = -1,
        private val random: Boolean = false,
        private val transitionType: TransitionType = TransitionType.STRAIGHT,
        private val transitionDirection: TransitionDirection = TransitionDirection.LEFT_RIGHT,
        private val transitionBlendMode: BlendMode = BlendMode.REPLACE,
        private val transitionDuration: Long = 2550,
        private val verbose: Boolean = false
    ) : Runnable, Thread("playable-loop-runner") {

        override fun run() {
            playable.play(
                xled = xled,
                loop = loop,
                random = random,
                transitionType = transitionType,
                transitionDirection = transitionDirection,
                transitionBlendMode = transitionBlendMode,
                transitionDuration = transitionDuration,
                verbose = verbose
            )
        }
    }

    fun play(
        xled: XLed,
        loop: Int = 1, // One shot
        random: Boolean = false,
        transitionType: TransitionType? = null,
        transitionDirection: TransitionDirection? = null,
        transitionBlendMode: BlendMode? = BlendMode.REPLACE,
        transitionDuration: Long = 2550,
        verbose: Boolean = false
    )


    fun stop() {
        running = false
    }

    fun frames(): List<Playable>

    fun firstFrame(): XledFrame? {
        return when (this) {
            is XledFrame -> this
            is XledSequence -> {
                when (val lf = frames().firstOrNull()) {
                    is XledFrame -> lf
                    is XledSequence -> lf.firstFrame()
                    else -> null
                }
            }
            else -> null
        }
    }

    fun lastFrame(): XledFrame? {
        return when (this) {
            is XledFrame -> this
            is XledSequence -> {
                when (val lf = frames().lastOrNull()) {
                    is XledFrame -> lf
                    is XledSequence -> lf.lastFrame()
                    else -> null
                }
            }
            else -> null
        }
    }

    fun toByteArray(bytesPerLed: Int): ByteArray
}
