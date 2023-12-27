package de.visualdigits.kotlin.twinklycontroller.configuration

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin

class XledArrayConfiguration(
    val deviceOrigin: DeviceOrigin,
    val columns: Array<XledDeviceColummn>
)

class XledDeviceColummn(
    val devices: Array<XledDeviceConfiguration>
)

class XledDeviceConfiguration(
    val name: String,
    val ipAddress: String,
    val width: String,
    val height: String,
)
