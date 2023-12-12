package de.visualdigits.kotlin.twinkly.model.device.xled.response.movies


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class Movies(
    code: Int? = null,
    val movies: List<Movie> = listOf(),
    @JsonProperty("available_frames") val availableFrames: Int? = null,
    @JsonProperty("max_capacity") val maxCapacity: Int? = null,
    val max: Int? = null
) : JsonObject(code)
