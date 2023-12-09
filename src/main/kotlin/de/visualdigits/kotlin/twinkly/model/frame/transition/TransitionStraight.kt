package de.visualdigits.kotlin.twinkly.model.frame.transition

import de.visualdigits.kotlin.twinkly.model.frame.XledFrame

class TransitionStraight: Transition() {

    override fun nextFrame(sourceFrame: XledFrame, targetFrame: XledFrame, factor: Double): XledFrame? = null
}
