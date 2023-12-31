package de.visualdigits.kotlin.twinkly.model.playable.transition

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame

class TransitionFade : Transition() {

    override fun nextFrame(
        sourceFrame: XledFrame,
        targetFrame: XledFrame,
        transitionDirection: TransitionDirection,
        blendMode: BlendMode,
        factor: Double
    ): XledFrame {
        return sourceFrame.fade(targetFrame, factor)
    }
}
