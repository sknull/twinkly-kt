package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class Input(
    val id: String = "",
    @JsonProperty("distribution_enable") val distributionEnable: Boolean = false,
    @JsonProperty("rename_enable") val renameEnable: Boolean = false,
    @JsonProperty("account_enable") val accountEnable: Boolean = false,
    @JsonProperty("play_info_type") val playInfoType: String = ""
)
