package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class McSurround(
    val version: Double = 0.0,
    @JsonProperty("func_list") val funcList: List<String> = listOf(),
    @JsonProperty("master_role") val masterRole: MasterRole = MasterRole(),
    @JsonProperty("slave_role") val slaveRole: SlaveRole = SlaveRole()
)
