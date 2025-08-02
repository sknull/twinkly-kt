package de.visualdigits.kotlin.twinkly.model.device.xled.response.music


import com.fasterxml.jackson.annotation.JsonProperty

class EffectsetsConfig(
    @JsonProperty("auto_switch") val autoSwitch: Int? = null,
    @JsonProperty("auto_mood") val autoMood: Int? = null
)
