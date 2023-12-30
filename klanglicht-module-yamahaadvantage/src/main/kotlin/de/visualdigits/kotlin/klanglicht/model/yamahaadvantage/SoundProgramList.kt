package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage


import com.fasterxml.jackson.annotation.JsonProperty

data class SoundProgramList(
    @JsonProperty("response_code") val responseCode: Int = 0,
    @JsonProperty("sound_program_list") val soundProgramList: List<String> = listOf()
)
