package de.visualdigits.kotlin.twinkly.model.common.networkstatus


import com.fasterxml.jackson.annotation.JsonProperty

class Ap(
    val ssid: String = "",
    val channel: Int = 0,
    val ip: String = "",
    val enc: Int = 0,
    @JsonProperty("ssid_hidden") val ssidHidden: Int = 0,
    @JsonProperty("max_connections") val maxConnections: Int = 0
)
