package de.visualdigits.kotlin.twinkly.model.device.xled.response.music


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class CurrentMusicDriversResponse(
    code: Int? = null,
    val handle: Int? = null,
    @JsonProperty("unique_id") val uniqueId: String? = null
) : JsonObject(code)
