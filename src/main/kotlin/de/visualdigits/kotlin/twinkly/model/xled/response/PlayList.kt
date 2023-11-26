package de.visualdigits.kotlin.twinkly.model.xled.response


import com.fasterxml.jackson.annotation.JsonProperty

data class PlayList(
    @JsonProperty("unique_id") val uniqueId: String? = null,
    val name: String? = null,
    val entries: List<Any> = listOf(),
    val code: Int? = null
)
