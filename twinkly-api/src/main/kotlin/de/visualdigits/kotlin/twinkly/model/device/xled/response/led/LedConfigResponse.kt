package de.visualdigits.kotlin.twinkly.model.device.xled.response.led

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.visualdigits.kotlin.twinkly.model.common.JsonObject


@JsonIgnoreProperties("attributes")
class LedConfigResponse(
    code: Int? = null,
    val strings: List<Map<String, Int>> = listOf(),
    val attributes: Map<String, Any> = strings.flatMap { it.map { e -> Pair(e.key, e.value) } }.toMap()
) : JsonObject(code)
