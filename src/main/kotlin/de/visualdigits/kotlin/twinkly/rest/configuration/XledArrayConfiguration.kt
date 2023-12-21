package de.visualdigits.kotlin.twinkly.rest.configuration

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin

class XledArrayConfiguration(
    val deviceOrigin: DeviceOrigin,
    val devices: Array<Array<XledDeviceConfiguration>>
)

class XledDeviceConfiguration(
    val name: String,
    val ipAddress: String,
    val width: String,
    val height: String,
)
