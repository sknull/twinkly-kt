package de.visualdigits.kotlin.twinkly.model.xled.response.musicenabled


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MusicEnabled(
    val enabled: Int? = null,
    val active: Int? = null,
    val mode: String? = null,
    @JsonProperty("auto_mode") val autoMode: String? = null,
    @JsonProperty("effectsets_config") val effectsetsConfig: EffectsetsConfig? = null,
    code: Int? = null
): JsonObject(code)
