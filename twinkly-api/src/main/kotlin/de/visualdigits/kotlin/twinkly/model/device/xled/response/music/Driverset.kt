package de.visualdigits.kotlin.twinkly.model.device.xled.response.music


import com.fasterxml.jackson.annotation.JsonProperty

class Driverset(
    val id: Int? = null,
    val count: Int? = null,
    @JsonProperty("unique_ids") val uniqueIds: List<String> = listOf()
)
