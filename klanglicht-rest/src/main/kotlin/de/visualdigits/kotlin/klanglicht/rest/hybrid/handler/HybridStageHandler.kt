package de.visualdigits.kotlin.klanglicht.rest.hybrid.handler

import de.visualdigits.kotlin.klanglicht.model.hybrid.HybridScene
import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class HybridStageHandler {

    @Autowired
    val configHolder: ConfigHolder? = null

     /**
     * Set hex colors.
     *
     * @param ids The comma separated list of ids.
     * @param hexColors The comma separated list of hex colors.
     * @param gains The comma separated list of gains (taken from stage setup if omitted).
     * @param transitionDuration The fade duration in milli seconds.
     * @param turnOn Determines if the device should be turned on.
     */
    fun hexColor(
        ids: String,
        hexColors: String,
        gains: String,
        transitionDuration: Long
    ) {
        val nextScene = HybridScene(ids, hexColors, gains, preferences = configHolder?.preferences)

         configHolder?.currentScene?.fade(nextScene, transitionDuration, configHolder.preferences!!)

         configHolder?.updateScene(nextScene)
    }
}
