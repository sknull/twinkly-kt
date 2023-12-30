package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class System(
    @JsonProperty("func_list") val funcList: List<String> = listOf(),
    @JsonProperty("zone_num") val zoneNum: Int = 0,
    @JsonProperty("input_list") val inputList: List<de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Input> = listOf(),
    val bluetooth: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Bluetooth = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Bluetooth(),
    @JsonProperty("web_control_url") val webControlUrl: String = "",
    @JsonProperty("party_volume_list") val partyVolumeList: List<String> = listOf(),
    @JsonProperty("hdmi_standby_through_list") val hdmiStandbyThroughList: List<String> = listOf()
)
