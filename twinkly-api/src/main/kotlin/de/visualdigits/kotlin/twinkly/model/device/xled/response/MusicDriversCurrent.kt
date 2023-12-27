package de.visualdigits.kotlin.twinkly.model.device.xled.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MusicDriversCurrent(
    code: Int? = null,
    val handle: Int? = null,
    @JsonProperty("unique_id") val uniqueId: String? = null
) : JsonObject(code)
