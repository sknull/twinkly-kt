package de.visualdigits.kotlin.twinkly.model.device.xmusic.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MicEnabledResponse(
    code: Int? = null,
    @JsonProperty("mic_enabled") val micEnabled: Int? = null
): JsonObject(code)
