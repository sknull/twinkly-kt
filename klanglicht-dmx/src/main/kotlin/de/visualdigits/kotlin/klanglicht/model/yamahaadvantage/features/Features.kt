package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class Features(
    @JsonProperty("response_code") val responseCode: Int = 0,
    val system: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.System = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.System(),
    val zone: List<de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Zone> = listOf(),
    val tuner: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Tuner = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Tuner(),
    val netusb: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Netusb = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Netusb(),
    val distribution: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Distribution = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Distribution(),
    val ccs: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Ccs = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Ccs()
)
