package de.visualdigits.kotlin.twinkly.model.xled.response

import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import java.util.UUID

class NewMovieResponse(
    code: Int? = null,
    val name: String? = null,
    @JsonProperty("unique_id") val uniqueId: String? = UUID.randomUUID().toString(),
    @JsonProperty("entry_point") val entryPoint: String? = null,
    @JsonProperty("id") val id: Int? = null,
    @JsonProperty("handle") val handle: Int? = null,
    val fps: Int? = null
) : JsonObject(code)
