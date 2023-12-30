package de.visualdigits.kotlin.klanglicht.model.shelly.status


class WifiState(
    val connected: Boolean? = null,
    val ssid: String? = null,
    val ip: String? = null,
    val rssi: Int? = null
)
