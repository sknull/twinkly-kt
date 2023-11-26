package de.visualdigits.kotlin.twinkly.model.xmusic.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MusicConfig(
    @JsonProperty("auto_mood_mode") val autoMoodMode: Int? = null,
    @JsonProperty("mood_index") val moodIndex: Int? = null,
    @JsonProperty("effect_index") val effectIndex: Int? = null,
    @JsonProperty("moods_number") val moodsNumber: Int? = null,
    @JsonProperty("led_mode") val ledMode: Int? = null,
    @JsonProperty("skip_off") val skipOff: Int? = null,
    @JsonProperty("keep_index") val keepIndex: Int? = null,
    code: Int? = null
): JsonObject(code)
