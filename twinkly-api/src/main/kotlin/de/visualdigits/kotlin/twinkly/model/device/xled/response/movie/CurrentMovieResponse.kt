package de.visualdigits.kotlin.twinkly.model.device.xled.response.movie

import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class CurrentMovieResponse(
    code: Int? = null,
    val id: Int? = null,
    @JsonProperty("unique_id") val uniqueId: String? = null,
    val name: String? = null
) : JsonObject(code)
