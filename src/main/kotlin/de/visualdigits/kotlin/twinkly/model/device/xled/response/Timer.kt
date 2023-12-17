package de.visualdigits.kotlin.twinkly.model.device.xled.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class Timer(
    code: Int = 0,
    @JsonProperty("time_now") val timeNow: Int = 0,
    @JsonProperty("time_on") val timeOn: Int = 0,
    @JsonProperty("time_off") val timeOff: Int = 0,
    val tz: String = ""
) : JsonObject(code)
