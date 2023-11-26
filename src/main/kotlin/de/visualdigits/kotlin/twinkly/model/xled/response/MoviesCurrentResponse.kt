package de.visualdigits.kotlin.twinkly.model.xled.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MoviesCurrentResponse(
    code: Int? = null,
    val id: Int? = null,
    @JsonProperty("unique_id") val uniqueId: String? = null,
    val name: String? = null
) : JsonObject(code)
