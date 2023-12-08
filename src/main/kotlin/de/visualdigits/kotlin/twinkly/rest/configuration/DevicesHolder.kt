package de.visualdigits.kotlin.twinkly.rest.configuration

import de.visualdigits.kotlin.twinkly.model.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.xled.XledArray
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DevicesHolder {

    private val log = LoggerFactory.getLogger(DevicesHolder::class.java)

    @Autowired
    private lateinit var properties: ApplicationProperties

    var xledArray: XledArray = XledArray(listOf())
    var xledDevices: Map<String, XLedDevice> = mapOf()

    @PostConstruct
    fun initialize() {
        log.info("XledArrayController TwinklyController...")
        xledDevices = properties.devices.map {
            Pair(it.key.substringAfter('.'), XLedDevice(it.value))
        }.toMap()
        log.info("Using devices '${xledDevices.keys}'")
        xledArray = XledArray(xledDevices.values.toList())
    }
}
