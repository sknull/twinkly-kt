package de.visualdigits.kotlin.twinkly.model.device.xled.response

import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class Effects(
    code: Int? = null,
    @JsonProperty("effects_number") val effectsNumber: Int? = null,
    @JsonProperty("unique_ids") val uniqueIds: List<String> = listOf()
) : JsonObject(code)
