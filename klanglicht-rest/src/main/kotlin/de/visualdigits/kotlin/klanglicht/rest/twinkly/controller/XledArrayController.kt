package de.visualdigits.kotlin.klanglicht.rest.twinkly.controller

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.twinkly.model.color.BlendMode
import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.model.playable.Playable
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionDirection
import de.visualdigits.kotlin.twinkly.model.playable.transition.TransitionType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
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
    var configHolder: ConfigHolder? = null

    private var playable: Playable? = null

    private var currentMode: DeviceMode = DeviceMode.off

    @GetMapping("/hello")
    fun hello(): String {
        return "hello"
    }

    @PutMapping("/power/on")
    fun powerOn() {
        log.info("Powering on")
        configHolder?.xledArray?.powerOn()
    }

    @PutMapping("/power/off")
    fun powerOff() {
        log.info("Powering off")
        configHolder?.xledArray?.powerOff()
    }

    @PutMapping("/brightness/{brightness}")
    fun setBrightness(
        @PathVariable brightness: Int,
    ) {
        log.info("Setting brightness to $brightness")
        configHolder?.xledArray?.setBrightness(Brightness(value = brightness))
    }

    @PutMapping("/saturation/{saturation}")
    fun setSaturation(
        @PathVariable saturation: Int,
    ) {
        log.info("Setting saturation to $saturation")
        configHolder?.xledArray?.setSaturation(Saturation(value = saturation))
    }

    @PutMapping("/mode/{mode}")
    fun setMode(
        @PathVariable mode: String,
    ) {
        log.info("Setting saturation to $mode")
        currentMode = DeviceMode.valueOf(mode)
        configHolder?.xledArray?.setMode(currentMode)
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
        configHolder?.xledArray?.setMode(DeviceMode.color)
        configHolder?.xledArray?.setColor(rgbwColor)
    }

    @PostMapping("/image")
    fun showImage(@RequestBody bytes: ByteArray) {
        if (playable != null && playable?.running == true) {
            stopLoop()
        }
// todo
//        currentMode = configHolder?.xledArray?.getMode()
        configHolder?.xledArray?.setMode(DeviceMode.rt)
        playable = XledFrame(bytes)
        configHolder?.xledArray?.also { playable?.playAsync(xled = it) }
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
// todo
//        currentMode = configHolder?.xledArray?.getMode()
        configHolder?.xledArray?.setMode(DeviceMode.rt)
        playable = XledSequence(frameDelay = frameDelay,
            directory = File(ClassLoader.getSystemResource(directory).toURI()))
        configHolder?.xledArray?.also {
            playable?.playAsync(
                xled = it,
                loop = loop,
                random = random,
                transitionType = transitionType,
                transitionDirection = transitionDirection,
                transitionBlendMode = transitionBlendMode,
                transitionDuration = transitionDuration
            )
        }
    }

    @PutMapping("/loop/stop")
    fun stopLoop() {
        configHolder?.xledArray?.setMode(currentMode)
        playable?.stop()
    }

    @GetMapping("/timer", produces = ["application/json"])
    fun getTimer(): Timer? {
        return configHolder?.xledArray?.getTimer()
    }

    @PostMapping("/timer", consumes = ["application/json"], produces = ["application/json"])
    fun setTimer(
        @RequestBody timer: Timer
    ): Timer? {
        return configHolder?.xledArray?.setTimer(timer)
    }
}
