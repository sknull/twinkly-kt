package de.visualdigits.kotlin.twinkly.model.device.xled.response


import com.fasterxml.jackson.annotation.JsonProperty

data class Sync(
    val mode: SyncMode? = null,
    @JsonProperty("slave_id") val slaveId: Int? = null,
    @JsonProperty("master_id") val masterId: Int? = null,
    @JsonProperty("compat_mode") val compatMode: Int? = null
)
