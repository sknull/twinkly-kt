package de.visualdigits.kotlin.twinkly.model.twinkly

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLed
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedArray
import kotlin.collections.MutableList

class TwinklyConfiguration(
    val name: String,
    val deviceOrigin: String,
    val gain: Double,
    val array: List<List<XledDeviceConfiguration>>
) {

    val xledArray: XLedArray

    init {
        val xLedDevices = array.map { column ->
            column.map { config ->
                XLedDevice.instance(
                    ipAddress = config.ipAddress,
                    name = "xled",
                    width = config.width,
                    height = config.height
                )
            }.toMutableList()
        }.toMutableList()
        xledArray = XLedArray.instance(
            xLedDevices = xLedDevices as MutableList<MutableList<XLed>>,
            deviceOrigin = DeviceOrigin.valueOf(deviceOrigin)
        )
    }
}
