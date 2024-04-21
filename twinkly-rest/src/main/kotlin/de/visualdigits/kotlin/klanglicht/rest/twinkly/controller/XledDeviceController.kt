package de.visualdigits.kotlin.klanglicht.rest.twinkly.controller

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/twinkly/api/v1/xleddevice")
class XledDeviceController {

    private val log = LoggerFactory.getLogger(XledDeviceController::class.java)

    @Autowired
    private var configHolder: ConfigHolder? = null

    @PutMapping("/{device}/power/on")
    fun powerOn(
        @PathVariable device: String
    ) {
        log.debug("Powering on $device")
        configHolder?.xledDevices?.get(device)?.powerOn()
    }

    @PutMapping("/{device}/power/off")
    fun powerOff(
        @PathVariable device: String
    ) {
        log.debug("Powering off $device")
        configHolder?.xledDevices?.get(device)?.powerOff()
    }

    @GetMapping("/{device}/brightness", produces = ["application/json"])
    fun getBrightness(
        @PathVariable device: String
    ): Float? {
        log.debug("Getting saturation")
        val brightness = configHolder?.xledDevices?.get(device)?.getBrightness()
        return brightness?.value?.div(100.0f)
    }

    @PutMapping("/{device}/brightness/{brightness}")
    fun setBrightness(
        @PathVariable device: String,
        @PathVariable brightness: Float,
    ) {
        log.debug("Setting brightness to $brightness")
        configHolder?.xledDevices?.get(device)?.setBrightness(brightness)
    }

    @GetMapping("/{device}/saturation", produces = ["application/json"])
    fun getSaturation(
        @PathVariable device: String
    ): Float? {
        log.debug("Getting saturation")
        val saturation = configHolder?.xledDevices?.get(device)?.getSaturation()
        return saturation?.value?.div(100.0f)
    }

    @PutMapping("/{device}/saturation/{saturation}")
    fun setSaturation(
        @PathVariable device: String,
        @PathVariable saturation: Float,
    ) {
        log.debug("Setting saturation to $saturation")
        configHolder?.xledDevices?.get(device)?.setSaturation(saturation)
    }

    @PutMapping("/{device}/mode/{mode}")
    fun setMode(
        @PathVariable device: String,
        @PathVariable mode: String,
    ) {
        log.debug("Setting saturation to $mode")
        configHolder?.xledDevices?.get(device)?.setMode(DeviceMode.valueOf(mode))
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
        log.debug("Showing color ${rgbwColor.ansiColor()}")
        configHolder?.xledDevices?.get(device)?.setMode(DeviceMode.color)
        configHolder?.xledDevices?.get(device)?.setColor(rgbwColor)
    }
}
