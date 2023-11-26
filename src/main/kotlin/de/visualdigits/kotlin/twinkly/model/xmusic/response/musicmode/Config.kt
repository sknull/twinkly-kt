package de.visualdigits.kotlin.twinkly.model.xmusic.response.musicmode


import com.fasterxml.jackson.annotation.JsonProperty

class Config(
    @JsonProperty("mood_index") val moodIndex: Int? = null,
    @JsonProperty("effect_index") val effectIndex: Int? = null
)
