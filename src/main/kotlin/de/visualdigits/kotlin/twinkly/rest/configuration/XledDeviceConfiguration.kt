package de.visualdigits.kotlin.twinkly.rest.configuration

import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin

class XledDeviceConfiguration(
    val ipAddress: String? = null,
    val deviceOrigin: DeviceOrigin? = null,
)
