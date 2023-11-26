package de.visualdigits.kotlin.twinkly.model.xled.response

import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class Saturation(
    code: Int? = null,
    val mode: String? = null,
    val value: Int? = null
) : JsonObject(code)
