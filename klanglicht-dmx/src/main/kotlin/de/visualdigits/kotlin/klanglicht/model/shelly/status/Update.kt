package de.visualdigits.kotlin.klanglicht.model.shelly.status

import com.fasterxml.jackson.annotation.JsonProperty


class Update(
    val status: String? = null,
    @JsonProperty("has_update") val hasUpdate: Boolean? = null,
    @JsonProperty("new_version") val newVersion: String? = null,
    @JsonProperty("old_version") val oldVersion: String? = null,
)
