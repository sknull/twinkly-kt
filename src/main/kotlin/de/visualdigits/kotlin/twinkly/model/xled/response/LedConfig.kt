package de.visualdigits.kotlin.twinkly.model.xled.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.visualdigits.kotlin.twinkly.model.common.JsonObject


@JsonIgnoreProperties("attributes")
class LedConfig(
    code: Int? = null,
    val strings: List<Map<String, Int>> = listOf(),
    val attributes: Map<String, Any> = strings.flatMap { it.map { e -> Pair(e.key, e.value) } }.toMap()
) : JsonObject(code)
