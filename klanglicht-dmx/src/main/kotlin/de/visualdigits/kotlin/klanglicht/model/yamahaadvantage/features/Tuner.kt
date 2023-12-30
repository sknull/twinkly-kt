package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class Tuner(
    @JsonProperty("func_list") val funcList: List<String> = listOf(),
    @JsonProperty("range_step") val rangeStep: List<de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.RangeStepX> = listOf(),
    val preset: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Preset = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Preset()
)
