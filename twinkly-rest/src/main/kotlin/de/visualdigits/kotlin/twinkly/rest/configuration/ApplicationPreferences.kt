package de.visualdigits.kotlin.twinkly.rest.configuration

import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedArray
import de.visualdigits.kotlin.twinkly.model.twinkly.TwinklyConfiguration
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration
import java.io.File
import java.nio.file.Paths

@Configuration
@ConfigurationProperties(prefix = "application")
@ConfigurationPropertiesScan
class ApplicationPreferences {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    val twinklyDirectory = File(System.getProperty("user.home"), ".twinkly")

    var theme: String = ""
    var twinkly: List<TwinklyConfiguration> = listOf()
    var twinklyMap: Map<String, TwinklyConfiguration> = mapOf()
    var xledArrays: Map<String, XLedArray> = mapOf()
    var xledDevices: Map<String, XLedDevice> = mapOf()

    @PostConstruct
    fun initialize() {
        twinklyMap = twinkly.associate { Pair(it.name, it) }
        log.info("## Twinkly devices: ${twinklyMap.keys}")

        val xledDevices: MutableMap<String, XLedDevice> = mutableMapOf()
        xledArrays = twinkly.associate { Pair(it.name, it.xledArray) }
        this.xledDevices = xledDevices

        log.info("#### ")
        log.info("## theme: $theme")
        log.info("## twinkly: $twinkly")
        log.info("#### setUp - end")
    }

    fun getXledDevice(id: String): XLedDevice? = xledDevices[id]

    fun getXledArray(id: String): XLedArray? = xledArrays[id]

    fun getXledArrays(): List<XLedArray> = xledArrays.values.toList()

    fun getAbsoluteResource(relativeResourePath: String): File {
        return Paths.get(twinklyDirectory.absolutePath, "resources", relativeResourePath).toFile()
    }
}
