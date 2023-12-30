package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class Bluetooth(
    @JsonProperty("update_cancelable") val updateCancelable: Boolean = false,
    @JsonProperty("tx_connectivity_type_max") val txConnectivityTypeMax: Int = 0
)
