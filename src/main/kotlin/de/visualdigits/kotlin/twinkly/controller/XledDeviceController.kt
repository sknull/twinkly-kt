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
@RequestMapping("/twinkly/api/v1/xleddevice")
class XledDeviceController {

    private val log = LoggerFactory.getLogger(XledDeviceController::class.java)

    @Autowired
    private lateinit var properties: ApplicationProperties

    private var devices: Map<String, XLedDevice> = mapOf()

    @PostConstruct
    fun initialize() {
        log.info("Initializing XledDeviceController...")
        log.info("Using devices '${properties.devices}'")
        devices = properties.devices.map {
            Pair(it.key.substringAfter('.'), XLedDevice(it.value))
        }.toMap()
        log.info("Using devices '${devices.keys}'")
    }

    @PutMapping("/{device}/power/on")
    fun powerOn(
        @PathVariable device: String
    ) {
        log.info("Powering on $device")
        devices[device]?.powerOn()
    }

    @PutMapping("/{device}/power/off")
    fun powerOff(
        @PathVariable device: String
    ) {
        log.info("Powering off $device")
        devices[device]?.powerOff()
    }

    @GetMapping("/{device}/brightness", produces = ["application/json"])
    fun getBrightness(
        @PathVariable device: String
    ): Brightness? {
        log.info("Getting saturation")
        return devices[device]?.brightness()
    }

    @PutMapping("/{device}/brightness/{brightness}")
    fun setBrightness(
        @PathVariable device: String,
        @PathVariable brightness: Int,
    ) {
        log.info("Setting brightness to $brightness")
        devices[device]?.brightness(Brightness(value = brightness))
    }

    @GetMapping("/{device}/saturation", produces = ["application/json"])
    fun getSaturation(
        @PathVariable device: String
    ): Saturation? {
        log.info("Getting saturation")
        return devices[device]?.saturation()
    }

    @PutMapping("/{device}/saturation/{saturation}")
    fun setSaturation(
        @PathVariable device: String,
        @PathVariable saturation: Int,
    ) {
        log.info("Setting saturation to $saturation")
        devices[device]?.saturation(Saturation(value = saturation))
    }

    @PutMapping("/{device}/mode/{mode}")
    fun setMode(
        @PathVariable device: String,
        @PathVariable mode: String,
    ) {
        log.info("Setting saturation to $mode")
        devices[device]?.mode(DeviceMode.valueOf(mode))
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
        devices[device]?.mode(DeviceMode.color)
        devices[device]?.color(rgbwColor)
    }
}