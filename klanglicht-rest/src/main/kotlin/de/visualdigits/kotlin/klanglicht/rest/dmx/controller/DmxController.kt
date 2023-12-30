package de.visualdigits.kotlin.klanglicht.rest.dmx.controller

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.dmx.handler.DmxHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for DMX devices.
 */
@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/v1")
class DmxController {

    @Autowired
    val dmxHandler: DmxHandler? = null

    @Autowired
    val configHolder: ConfigHolder? = null

    @GetMapping("/colors")
    fun colors(
        @RequestParam(value = "hexColors") hexColors: String,
        @RequestParam(value = "gains", required = false, defaultValue = "1.0") gains: String,
        @RequestParam(value = "baseChannels", required = false, defaultValue = "") baseChannels: String,
        @RequestParam(value = "fadeDuration", required = false, defaultValue = "1000") fadeDuration: Long,
        @RequestParam(value = "stepDuration", required = false, defaultValue = "0") stepDuration: Long,
        @RequestParam(value = "transformationName", required = false, defaultValue = "FADE") transformationName: String,
        @RequestParam(value = "loop", required = false, defaultValue = "false") loop: Boolean,
        @RequestParam(value = "id", required = false, defaultValue = "id") id: String
    ) {
        // todo workaround for my current lmair configuration
        val ids = if (id == "Dmx") {
            configHolder?.preferences?.dmx?.devices?.map { it.baseChannel }?.joinToString(",")?:""
        } else {
            baseChannels
        }
        dmxHandler!!.hexColors(
            ids,
            hexColors,
            gains,
            fadeDuration
        )
    }
}
