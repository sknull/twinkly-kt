package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class Features(
    @JsonProperty("response_code") val responseCode: Int = 0,
    val system: System = System(),
    val zone: List<Zone> = listOf(),
    val tuner: Tuner = Tuner(),
    val netusb: Netusb = Netusb(),
    val distribution: Distribution = Distribution(),
    val ccs: Ccs = Ccs()
)
