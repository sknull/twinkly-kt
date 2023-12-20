package de.visualdigits.kotlin.twinkly.rest.configuration

import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DevicesHolder {

    private val log = LoggerFactory.getLogger(DevicesHolder::class.java)

    @Autowired
    private lateinit var properties: ApplicationProperties

    var xledArray: XledArray = XledArray(arrayOf())
    var xledDevices: Map<String, XLedDevice> = mapOf()

    @PostConstruct
    fun initialize() {
        log.info("XledArrayController TwinklyController...")
        xledDevices = properties.devices.map {
            val value = it.value
            Pair(it.key.substringAfter('.'), XLedDevice(value.ipAddress!!, width = 0, height = 0))
        }.toMap()
        log.info("Using devices '${xledDevices.keys}'")

        // todo
//        xledArray = XledArray(xledDevices.values.toList())
    }
}
