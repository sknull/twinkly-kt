package de.visualdigits.kotlin.klanglicht.rest.dmx.handler

import de.visualdigits.kotlin.klanglicht.model.hybrid.HybridScene
import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DmxHandler {

    @Autowired
    val configHolder: ConfigHolder? = null

    /**
     * Set hex colors.
     *
     * @param baseChannels The list of ids.
     * @param hexColors The list of hex colors.
     * @param gains The list of gains (taken from stage setup if omitted).
     * @param transitionDuration The fade duration in milli seconds.
     * @param stepDuration The duration of one transition step in milli seconds.
     * @param transformationName The transformation to use.
     */
    fun hexColors(
        baseChannels: String,
        hexColors: String,
        gains: String,
        transitionDuration: Long
    ) {
        val nextScene = HybridScene(baseChannels, hexColors, gains, preferences = configHolder?.preferences)

        configHolder?.currentScene?.fade(nextScene, transitionDuration, configHolder.preferences!!)

        configHolder?.updateScene(nextScene)
    }
}
