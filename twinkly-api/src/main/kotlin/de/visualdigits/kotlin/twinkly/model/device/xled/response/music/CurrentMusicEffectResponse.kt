package de.visualdigits.kotlin.twinkly.model.device.xled.response.music


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class CurrentMusicEffectResponse(
    code: Int? = null,
    @JsonProperty("effect_idx") val effectIdx: Int? = null,
    @JsonProperty("effect_uuid") val effectUuid: String? = null,
    @JsonProperty("effectset_idx") val effectsetIdx: Int? = null,
    @JsonProperty("effectset_uuid") val effectsetUuid: String? = null,
    @JsonProperty("effectsuperset_idx") val effectsupersetIdx: Int? = null,
    @JsonProperty("effectsuperset_name") val effectsupersetName: String? = null,
    val mood: String? = null,
    @JsonProperty("mood_index") val moodIndex: Int? = null
) : JsonObject(code)
