package de.visualdigits.kotlin.twinkly.model.device.xled.response.mode


import com.fasterxml.jackson.annotation.JsonProperty

data class Sync(
    val mode: SyncMode? = null,
    @JsonProperty("slave_id") val slaveId: String? = null,
    @JsonProperty("master_id") val masterId: String? = null,
    @JsonProperty("compat_mode") val compatMode: String? = null
)
