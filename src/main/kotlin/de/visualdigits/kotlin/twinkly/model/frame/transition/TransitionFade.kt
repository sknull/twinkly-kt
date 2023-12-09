package de.visualdigits.kotlin.twinkly.model.frame.transition

import de.visualdigits.kotlin.twinkly.model.frame.XledFrame

class TransitionFade : Transition() {

    override fun nextFrame(sourceFrame: XledFrame, targetFrame: XledFrame, factor: Double): XledFrame {
        return sourceFrame.fade(targetFrame, factor)
    }
}
