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

    var xledArray: XledArray = XledArray()
    var xledDevices: Map<String, XLedDevice> = mapOf()

    @PostConstruct
    fun initialize() {
        log.info("XledArrayController TwinklyController...")
        xledArray = properties.array?.let {
            XledArray(
                deviceOrigin = it.deviceOrigin,
                xLedDevices = properties.array?.devices?.map { column ->
                    column.map { config ->
                        XLedDevice(host = config.ipAddress, config.width.toInt(), config.height.toInt())
                    }.toTypedArray()
                }?.toTypedArray() ?: arrayOf()
            )
        } ?: XledArray()
        xledDevices = properties.array?.devices?.flatten()?.map { config ->
            Pair(config.name, XLedDevice(host = config.ipAddress, config.width.toInt(), config.height.toInt()))
        }?.toMap()?:mapOf()
        log.info("Using devices '${xledDevices.keys}'")
    }
}
