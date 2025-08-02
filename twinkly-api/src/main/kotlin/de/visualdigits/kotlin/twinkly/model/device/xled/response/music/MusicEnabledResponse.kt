package de.visualdigits.kotlin.twinkly.model.device.xled.response.music


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MusicEnabledResponse(
    code: Int? = null,
    val enabled: Int? = null,
    val active: Int? = null,
    val mode: String? = null,
    @JsonProperty("auto_mode") val autoMode: String? = null,
    @JsonProperty("effectsets_config") val effectsetsConfig: EffectsetsConfig? = null
): JsonObject(code)
