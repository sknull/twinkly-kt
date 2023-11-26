package de.visualdigits.kotlin.twinkly.model.xmusic.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MicEnabled(
    @JsonProperty("mic_enabled") val micEnabled: Int? = null,
    code: Int? = null
): JsonObject(code)
