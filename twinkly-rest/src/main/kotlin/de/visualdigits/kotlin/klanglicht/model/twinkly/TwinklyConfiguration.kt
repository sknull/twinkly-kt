package de.visualdigits.kotlin.klanglicht.model.twinkly

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray

class TwinklyConfiguration(
    val name: String,
    val deviceOrigin: String,
    val gain: Double,
    val array: Array<Array<XledDeviceConfiguration>>
) {

    val xledArray: XledArray

    init {
        xledArray = XledArray(
            deviceOrigin = DeviceOrigin.valueOf(deviceOrigin),
            xLedDevices = array.map { column ->
                column.map { config ->
                    XLedDevice(
                        host = config.ipAddress,
                        width = config.width,
                        height = config.height
                    )
                }.toTypedArray()
            }.toTypedArray()
        )
    }
}
