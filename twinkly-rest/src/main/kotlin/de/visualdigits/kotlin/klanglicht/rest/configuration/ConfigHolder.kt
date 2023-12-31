package de.visualdigits.kotlin.klanglicht.rest.configuration

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.SystemUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Paths

@Component
class ConfigHolder {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    var preferences: Preferences? = null

    val twinklyDirectory: File = File(SystemUtils.getUserHome(), ".twinkly")

    var xledArray: XledArray? = null
    var xledDevices: Map<String, XLedDevice> = mapOf()

    @PostConstruct
    fun initialize() {
        // load preferences
        log.info("#### setUp - start")
        preferences = Preferences.load(twinklyDirectory)

        // initialize twinkly devices
        val twinkly = preferences?.twinkly
        val deviceOrigin = twinkly?.deviceOrigin?.let { DeviceOrigin.valueOf(it) }?: DeviceOrigin.TOP_LEFT
        val xledDevices: MutableMap<String, XLedDevice> = mutableMapOf()
        xledArray = twinkly?.array?.map { column ->
            column.map { config ->
                val xledDevice = XLedDevice(host = config.ipAddress, config.width, config.height)
                xledDevices[config.name] = xledDevice
                xledDevice
            }.toTypedArray()
        }?.toTypedArray()
            ?.let { devices ->
                XledArray(deviceOrigin = deviceOrigin, xLedDevices = devices)
            }
        this.xledDevices = xledDevices
        log.info("##### Using twinkly devices '${xledDevices.keys}'")
        log.info("#### setUp - end")
    }

    fun getAbsoluteResource(relativeResourePath: String): File {
        return Paths.get(twinklyDirectory.absolutePath, "resources", relativeResourePath).toFile()
    }
}
