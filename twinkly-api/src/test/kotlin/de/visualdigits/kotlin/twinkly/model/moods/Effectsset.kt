package de.visualdigits.kotlin.twinkly.model.moods


import com.fasterxml.jackson.annotation.JsonProperty

data class Effectsset(
    @JsonProperty("id") val id: Int? = null,
    @JsonProperty("uuid") val uuid: String? = null,
    @JsonProperty("count") val count: Int? = null,
    @JsonProperty("unique_ids") val uniqueIds: List<String> = listOf()
)
