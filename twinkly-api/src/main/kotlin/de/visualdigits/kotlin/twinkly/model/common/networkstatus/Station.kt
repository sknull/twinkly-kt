package de.visualdigits.kotlin.twinkly.model.common.networkstatus


import com.fasterxml.jackson.annotation.JsonProperty

class Station(
    val ssid: String = "",
    @JsonProperty("connected_bssid") val connectedBssid: String = "",
    @JsonProperty("monitor_enabled") val monitorEnabled: Int = 0,
    val ip: String = "",
    val gw: String = "",
    val mask: String = "",
    val rssi: Int = 0
)
