package de.visualdigits.kotlin.twinkly.rest.controller

import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.Playable
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionDirection
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionType
import de.visualdigits.kotlin.twinkly.rest.configuration.DevicesHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File

@RestController()
@RequestMapping("/twinkly/api/v1/xledarray")
class XledArrayController {

    private val log = LoggerFactory.getLogger(XledArrayController::class.java)

    @Autowired
    private lateinit var devicesHolder: DevicesHolder

    private var playable: Playable? = null

    private var currentMode: DeviceMode = DeviceMode.off

    @PutMapping("/power/on")
    fun powerOn() {
        log.info("Powering on")
        devicesHolder.xledArray.powerOn()
    }

    @PutMapping("/power/off")
    fun powerOff() {
        log.info("Powering off")
        devicesHolder.xledArray.powerOff()
    }

    @PutMapping("/brightness/{brightness}")
    fun setBrightness(
        @PathVariable brightness: Int,
    ) {
        log.info("Setting brightness to $brightness")
        devicesHolder.xledArray.setBrightness(Brightness(value = brightness))
    }

    @PutMapping("/saturation/{saturation}")
    fun setSaturation(
        @PathVariable saturation: Int,
    ) {
        log.info("Setting saturation to $saturation")
        devicesHolder.xledArray.setSaturation(Saturation(value = saturation))
    }

    @PutMapping("/mode/{mode}")
    fun setMode(
        @PathVariable mode: String,
    ) {
        log.info("Setting saturation to $mode")
        currentMode = DeviceMode.valueOf(mode)
        devicesHolder.xledArray.setMode(currentMode)
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
        devicesHolder.xledArray.setMode(DeviceMode.color)
        devicesHolder.xledArray.setColor(rgbwColor)
    }

    @PostMapping("/image")
    fun showImage(@RequestBody bytes: ByteArray) {
        if (playable != null && playable?.running == true) {
            stopLoop()
        }
        currentMode = devicesHolder.xledArray.getMode()
        devicesHolder.xledArray.setMode(DeviceMode.rt)
        playable = XledFrame(bytes)
        playable?.playAsync(
            xled = devicesHolder.xledArray,
        )
    }

    @PostMapping("/sequence")
    fun showSequence(
        @RequestParam(required = false, defaultValue = "-1") loop: Int,
        @RequestParam(required = false, defaultValue = "false") random: Boolean,
        @RequestParam(required = false, defaultValue = "STRAIGHT") transitionType: TransitionType,
        @RequestParam(required = false, defaultValue = "LEFT_RIGHT") transitionDirection: TransitionDirection,
        @RequestParam(required = false, defaultValue = "REPLACE") transitionBlendMode: BlendMode,
        @RequestParam(required = false, defaultValue = "2550") transitionDuration: Long,
        @RequestParam(required = false, defaultValue = "100") frameDelay: Long,
        @RequestBody directory: String
    ) {
        if (playable != null && playable?.running == true) {
            stopLoop()
        }
        currentMode = devicesHolder.xledArray.getMode()
        devicesHolder.xledArray.setMode(DeviceMode.rt)
        playable = XledSequence(frameDelay = frameDelay,
            directory = File(ClassLoader.getSystemResource(directory).toURI()))
        playable?.playAsync(
            xled = devicesHolder.xledArray,
            loop = loop,
            random = random,
            transitionType = transitionType,
            transitionDirection = transitionDirection,
            transitionBlendMode = transitionBlendMode,
            transitionDuration = transitionDuration
        )
    }

    @PutMapping("/loop/stop")
    fun stopLoop() {
        devicesHolder.xledArray.setMode(currentMode)
        playable?.stop()
    }
}
