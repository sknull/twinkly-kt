package de.visualdigits.kotlin.twinkly.model.xled.response


import com.fasterxml.jackson.annotation.JsonProperty

class MusicDriversCurrent(
    val handle: Int? = null,
    @JsonProperty("unique_id") val uniqueId: String? = null,
    val code: Int? = null
)
