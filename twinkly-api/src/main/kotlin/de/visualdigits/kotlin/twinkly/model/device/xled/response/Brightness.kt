package de.visualdigits.kotlin.twinkly.model.device.xled.response

import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class Brightness(
    code: Int? = null,
    val mode: String? = null,
    val value: Int? = null
) : JsonObject(code)
