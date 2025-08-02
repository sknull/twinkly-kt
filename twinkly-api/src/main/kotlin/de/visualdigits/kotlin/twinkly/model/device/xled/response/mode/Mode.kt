package de.visualdigits.kotlin.twinkly.model.device.xled.response.mode

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

@JsonIgnoreProperties("deviceMode")
class Mode(
    code: Int? = null,
    val mode: String? = null,
    @JsonProperty("detect_mode") val detectMode: Int? = null,
    @JsonProperty("shop_mode") val shopMode: Int? = null,
    @JsonProperty("color_config") val colorConfig: ColorConfig? = null,
    val id: Int? = null,
    @JsonProperty("unique_id") val uniqueId: String? = null,
    @JsonProperty("effect_id") val effectId: Int? = null,
    @JsonProperty("effect_config") val effectConfig: EffectConfig? = null,
    val name: String? = null,
    val ledMode: LedMode = mode?.let { m -> LedMode.valueOf(m) }?: LedMode.off
) : JsonObject(code)
