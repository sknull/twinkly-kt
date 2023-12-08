package de.visualdigits.kotlin.twinkly.rest.controller

import de.visualdigits.kotlin.twinkly.model.color.RGBWColor
import de.visualdigits.kotlin.twinkly.model.frame.Playable
import de.visualdigits.kotlin.twinkly.model.frame.XledFrame
import de.visualdigits.kotlin.twinkly.model.xled.XLed
import de.visualdigits.kotlin.twinkly.model.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.xled.response.mode.DeviceMode
import de.visualdigits.kotlin.twinkly.rest.configuration.DevicesHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
        devicesHolder.xledArray.brightness(Brightness(value = brightness))
    }

    @PutMapping("/saturation/{saturation}")
    fun setSaturation(
        @PathVariable saturation: Int,
    ) {
        log.info("Setting saturation to $saturation")
        devicesHolder.xledArray.saturation(Saturation(value = saturation))
    }

    @PutMapping("/mode/{mode}")
    fun setMode(
        @PathVariable mode: String,
    ) {
        log.info("Setting saturation to $mode")
        currentMode = DeviceMode.valueOf(mode)
        devicesHolder.xledArray.mode(currentMode)
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
        devicesHolder.xledArray.mode(DeviceMode.color)
        devicesHolder.xledArray.color(rgbwColor)
    }

    @PostMapping("/image")
    fun showImage(@RequestBody bytes: ByteArray) {
        currentMode = devicesHolder.xledArray.mode()
        devicesHolder.xledArray.mode(DeviceMode.rt)
        playable = XledFrame.fromImage(bytes)
        Thread(LoopRunner(
            devicesHolder.xledArray,
            playable!!
        )).start()
    }

    @PutMapping("/loop/stop")
    fun stopLoop() {
        devicesHolder.xledArray.mode(currentMode)
        playable?.stop()
    }

    class LoopRunner(
        val xled: XLed,
        val playable: Playable
    ): Runnable {
        override fun run() {
            playable.play(xled, 5000)
        }

    }
}
