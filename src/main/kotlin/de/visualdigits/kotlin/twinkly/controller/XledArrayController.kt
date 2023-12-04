package de.visualdigits.kotlin.twinkly.controller

import de.visualdigits.kotlin.twinkly.configuration.ApplicationProperties
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.xled.XledArray
import de.visualdigits.kotlin.twinkly.model.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/twinkly/api/v1/xledarray")
class XledArrayController {

    private val log = LoggerFactory.getLogger(XledArrayController::class.java)

    @Autowired
    private lateinit var properties: ApplicationProperties

    private var xledArray: XledArray = XledArray(listOf())
    private var devices: Map<String, XLedDevice> = mapOf()


    @PostConstruct
    fun initialize() {
        log.info("XledArrayController TwinklyController...")
        devices = properties.devices.map {
            Pair(it.key.substringAfter('.'), XLedDevice(it.value))
        }.toMap()
        log.info("Using devices '${devices.keys}'")
        xledArray = XledArray(devices.values.toList())
    }

    @PutMapping("/power/on")
    fun powerOn() {
        log.info("Powering on")
        xledArray.powerOn()
    }

    @PutMapping("/power/off")
    fun powerOff() {
        log.info("Powering off")
        xledArray.powerOff()
    }

    @PutMapping("/brightness/{brightness}")
    fun setBrightness(
        @PathVariable brightness: Int,
    ) {
        log.info("Setting brightness to $brightness")
        xledArray.brightness(Brightness(value = brightness))
    }

    @PutMapping("/saturation/{saturation}")
    fun setSaturation(
        @PathVariable saturation: Int,
    ) {
        log.info("Setting saturation to $saturation")
        xledArray.saturation(Saturation(value = saturation))
    }

    @PutMapping("/mode/{mode}")
    fun setMode(
        @PathVariable mode: String,
    ) {
        log.info("Setting saturation to $mode")
        xledArray.mode(DeviceMode.valueOf(mode))
    }

    @PutMapping("/color/{red}/{green}/{blue}/{white}")
    fun setColor(
        @PathVariable red: Int,
        @PathVariable green: Int,
        @PathVariable blue: Int,
        @PathVariable white: Int,
    ) {
        val rgbwColor = RGBWColor(red, green, blue, white)
        log.info("Showing color ${rgbwColor.ansiColor()}")
        xledArray.mode(DeviceMode.color)
        xledArray.color(rgbwColor)
    }
}