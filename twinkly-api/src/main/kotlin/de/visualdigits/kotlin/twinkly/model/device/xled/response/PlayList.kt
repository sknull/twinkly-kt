package de.visualdigits.kotlin.twinkly.model.device.xled.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class PlayList(
    code: Int? = null,
    @JsonProperty("unique_id") val uniqueId: String? = null,
    val name: String? = null,
    val entries: List<Any> = listOf()
) : JsonObject(code)
