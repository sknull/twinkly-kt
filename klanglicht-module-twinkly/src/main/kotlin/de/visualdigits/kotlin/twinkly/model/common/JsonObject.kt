package de.visualdigits.kotlin.twinkly.model.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode

@JsonIgnoreProperties("responseCode")
open class JsonObject(
    val code: Int? = null,
    val responseCode: ResponseCode = code?.let { c -> ResponseCode.byCode(c) }?: ResponseCode.Unknown
) : JsonBaseObject()
