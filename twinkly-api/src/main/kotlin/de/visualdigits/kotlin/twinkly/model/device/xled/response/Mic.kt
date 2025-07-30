package de.visualdigits.kotlin.twinkly.model.device.xled.response


import com.fasterxml.jackson.annotation.JsonProperty

data class Mic(
    val filters: List<String>? = null,
    @JsonProperty("brightness_depth") val brightnessDepth: Int? = null,
    @JsonProperty("hue_depth") val hueDepth: Int? = null,
    @JsonProperty("value_depth") val valueDepth: Int? = null,
    @JsonProperty("saturation_depth") val saturationDepth: Int? = null
)
