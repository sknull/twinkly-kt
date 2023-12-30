package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class MasterRole(
    @JsonProperty("surround_pair") val surroundPair: Boolean = false,
    @JsonProperty("stereo_pair") val stereoPair: Boolean = false,
    @JsonProperty("subwoofer_pair") val subwooferPair: Boolean = false
)
