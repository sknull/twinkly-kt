package de.visualdigits.kotlin.twinkly.model.device.xled.response.led

import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class CurrentLedEffectResponse(
    code: Int? = null,
    @JsonProperty("preset_id") val presetId: Int? = null,
    @JsonProperty("unique_id") val uniqueId: String? = null
) : JsonObject(code)
