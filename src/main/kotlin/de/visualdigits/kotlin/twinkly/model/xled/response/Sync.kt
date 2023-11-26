package de.visualdigits.kotlin.twinkly.model.xled.response


import com.fasterxml.jackson.annotation.JsonProperty

data class Sync(
    val mode: String? = null,
    @JsonProperty("compat_mode") val compatMode: Int? = null
)
