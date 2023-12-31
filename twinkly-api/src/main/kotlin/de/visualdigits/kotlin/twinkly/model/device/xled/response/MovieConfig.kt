package de.visualdigits.kotlin.twinkly.model.device.xled.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MovieConfig(
    code: Int? = null,
    @JsonProperty("frame_delay") val frameDelay: Int? = null,
    @JsonProperty("frames_number") val framesNumber: Int? = null,
    @JsonProperty("leds_number") val ledsNumber: Int? = null
) : JsonObject(code)
