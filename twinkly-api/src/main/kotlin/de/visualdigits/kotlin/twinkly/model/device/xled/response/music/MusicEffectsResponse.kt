package de.visualdigits.kotlin.twinkly.model.device.xled.response.music

import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MusicEffectsResponse(
    code: Int? = null,
    @JsonProperty("effects_number") val effectsNumber: Int? = null,
    @JsonProperty("effects_uuids") val effectsUuids: List<String> = listOf()
) : JsonObject(code)
