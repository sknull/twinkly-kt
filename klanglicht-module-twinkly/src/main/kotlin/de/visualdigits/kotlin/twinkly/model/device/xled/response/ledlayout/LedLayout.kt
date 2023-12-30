package de.visualdigits.kotlin.twinkly.model.device.xled.response.ledlayout

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.visualdigits.kotlin.twinkly.model.common.JsonObject


@JsonIgnoreProperties("rows", "columns")
class LedLayout(
    code: Int? = null,
    val source: String? = null,
    val synthesized: Boolean = false,
    val uuid: String? = null,
    val coordinates: List<Coordinate> = listOf(),
    val rows: Int = coordinates.withIndex().find { it.value.y == 0.0 }?.let { it.index + 1 }?:1,
    val columns: Int = coordinates.size / rows
) : JsonObject(code)
