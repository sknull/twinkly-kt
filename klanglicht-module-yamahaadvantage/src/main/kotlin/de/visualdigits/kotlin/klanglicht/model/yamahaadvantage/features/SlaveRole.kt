package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class SlaveRole(
    @JsonProperty("surround_pair_l_or_r") val surroundPairLOrR: Boolean = false,
    @JsonProperty("surround_pair_lr") val surroundPairLr: Boolean = false,
    @JsonProperty("subwoofer_pair") val subwooferPair: Boolean = false
)
