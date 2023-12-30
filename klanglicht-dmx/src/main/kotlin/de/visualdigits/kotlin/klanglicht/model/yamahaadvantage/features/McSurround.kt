package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class McSurround(
    val version: Double = 0.0,
    @JsonProperty("func_list") val funcList: List<String> = listOf(),
    @JsonProperty("master_role") val masterRole: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.MasterRole = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.MasterRole(),
    @JsonProperty("slave_role") val slaveRole: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.SlaveRole = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.SlaveRole()
)
