package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage

import com.fasterxml.jackson.annotation.JsonProperty


class ResponseCode(
    @JsonProperty("response_code") val responseCode: Int = 0
)

