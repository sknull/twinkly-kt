package de.visualdigits.kotlin.klanglicht.rest.shelly.controller

import de.visualdigits.kotlin.klanglicht.rest.shelly.handler.ShellyHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for shelly devices.
 */
@RestController
@RequestMapping("/v1/shelly")
class ShellyRestController {

    @Autowired
    var shellyHandler: ShellyHandler? = null

    /**
     * Sets the given scene or index on the connected lightmanager air.
     *
     * @param sceneId
     * @param index
     */
    @GetMapping("/control")
    fun control(
        @RequestParam(value = "scene", required = false, defaultValue = "0") sceneId: Int,
        @RequestParam(value = "index", required = false, defaultValue = "0") index: Int
    ) {
        shellyHandler!!.control(sceneId, index)
    }

    @GetMapping("hexColor")
    fun hexColor(
        @RequestParam(value = "ids", required = false, defaultValue = "") ids: String,
        @RequestParam(value = "hexColors") hexColors: String,
        @RequestParam(value = "gains", required = false, defaultValue = "1.0") gains: String,
        @RequestParam(value = "transition", required = false, defaultValue = "1000") transitionDuration: Long,
        @RequestParam(value = "turnOn", required = false, defaultValue = "true") turnOn: Boolean,
        @RequestParam(value = "store", required = false, defaultValue = "true") store: Boolean
    ) {
        shellyHandler!!.hexColors(ids, hexColors, gains, transitionDuration, turnOn, store)
    }

    @GetMapping("color")
    fun color(
        @RequestParam(value = "ids", required = false, defaultValue = "") ids: String,
        @RequestParam(value = "red", required = false, defaultValue = "0") red: Int,
        @RequestParam(value = "green", required = false, defaultValue = "0") green: Int,
        @RequestParam(value = "blue", required = false, defaultValue = "0") blue: Int,
        @RequestParam(value = "gains", required = false, defaultValue = "1.0") gains: String,
        @RequestParam(value = "transition", required = false, defaultValue = "1000") transitionDuration: Long,
        @RequestParam(value = "turnOn", required = false, defaultValue = "true") turnOn: Boolean,
        @RequestParam(value = "store", required = false, defaultValue = "true") store: Boolean
    ) {
        shellyHandler!!.color(ids, red, green, blue, gains, transitionDuration, turnOn, store)
    }

    @GetMapping("restore")
    fun restoreColors(
        @RequestParam(value = "ids", required = false, defaultValue = "") ids: String,
        @RequestParam(value = "transition", required = false, defaultValue = "2000") transitionDuration: Long
    ) {
        shellyHandler!!.restoreColors(ids, transitionDuration)
    }

    @GetMapping("power")
    fun power(
        @RequestParam(value = "ids", required = false, defaultValue = "") ids: String,
        @RequestParam(value = "turnOn", required = false, defaultValue = "true") turnOn: Boolean,
        @RequestParam(value = "transition", required = false, defaultValue = "1000") transitionDuration: Long
    ) {
        shellyHandler!!.power(ids, turnOn, transitionDuration)
    }

    @GetMapping("gain")
    fun gain(
        @RequestParam(value = "ids", required = false, defaultValue = "") ids: String,
        @RequestParam(value = "gain", required = false, defaultValue = "1.0") gain: Int,
        @RequestParam(value = "transition", required = false, defaultValue = "1000") transitionDuration: Long
    ) {
        shellyHandler!!.gain(ids, gain, transitionDuration)
    }
}
