package de.visualdigits.kotlin.klanglicht.model.shelly.status

import com.fasterxml.jackson.annotation.JsonProperty


class Temperature(
    @JsonProperty("tC") val celsius: Double? = null,
    @JsonProperty("tF") val fahrenheit: Double? = null,
    @JsonProperty("is_valid") val isValid: Boolean? = null
)
