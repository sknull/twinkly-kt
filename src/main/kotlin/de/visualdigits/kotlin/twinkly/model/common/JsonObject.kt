package de.visualdigits.kotlin.twinkly.model.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import de.visualdigits.kotlin.twinkly.model.xled.response.ResponseCode
import kotlin.String

@JsonIgnoreProperties("responseCode")
open class JsonObject(
    val code: Int? = null,
    val responseCode: ResponseCode = code?.let { c -> ResponseCode.byCode(c) }?: ResponseCode.Unknown
) : JsonBaseObject()
