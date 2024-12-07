package de.visualdigits.kotlin.klanglicht.rest.twinkly.controller

import de.visualdigits.kotlin.klanglicht.rest.configuration.ApplicationPreferences
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/twinkly/api/v1/xleddevice")
class XledDeviceController(
    private val prefs: ApplicationPreferences
) {

    private val log = LoggerFactory.getLogger(XledDeviceController::class.java)

    @PutMapping("/{device}/power/on")
    fun powerOn(
        @PathVariable device: String
    ) {
        log.info("Powering on $device")
        prefs.preferences?.getXledDevice(device)?.powerOn()
    }

    @PutMapping("/{device}/power/off")
    fun powerOff(
        @PathVariable device: String
    ) {
        log.info("Powering off $device")
        prefs.preferences?.getXledDevice(device)?.powerOff()
    }

    @GetMapping("/{device}/brightness", produces = ["application/json"])
    fun getBrightness(
        @PathVariable device: String
    ): Brightness? {
        log.info("Getting saturation")
        return prefs.preferences?.getXledDevice(device)?.getBrightness()
    }

    @PutMapping("/{device}/brightness/{brightness}")
    fun setBrightness(
        @PathVariable device: String,
        @PathVariable brightness: Float,
    ) {
        log.info("Setting brightness to $brightness")
        prefs.preferences?.getXledDevice(device)?.setBrightness(brightness)
    }

    @GetMapping("/{device}/saturation", produces = ["application/json"])
    fun getSaturation(
        @PathVariable device: String
    ): Saturation? {
        log.info("Getting saturation")
        return prefs.preferences?.getXledDevice(device)?.getSaturation()
    }

    @PutMapping("/{device}/saturation/{saturation}")
    fun setSaturation(
        @PathVariable device: String,
        @PathVariable saturation: Float,
    ) {
        log.info("Setting saturation to $saturation")
        prefs.preferences?.getXledDevice(device)?.setSaturation(saturation)
    }

    @PutMapping("/{device}/mode/{mode}")
    fun setMode(
        @PathVariable device: String,
        @PathVariable mode: String,
    ) {
        log.info("Setting saturation to $mode")
        prefs.preferences?.getXledDevice(device)?.setMode(DeviceMode.valueOf(mode))
    }

    @PutMapping("/{device}/color/{red}/{green}/{blue}/{white}")
    fun setColor(
        @PathVariable device: String,
        @PathVariable red: Int,
        @PathVariable green: Int,
        @PathVariable blue: Int,
        @PathVariable white: Int,
    ) {
        val rgbwColor = RGBWColor(red, green, blue, white)
        log.info("Showing color ${rgbwColor.ansiColor()}")
        prefs.preferences?.getXledDevice(device)?.setMode(DeviceMode.color)
        prefs.preferences?.getXledDevice(device)?.setColor(rgbwColor)
    }
}
