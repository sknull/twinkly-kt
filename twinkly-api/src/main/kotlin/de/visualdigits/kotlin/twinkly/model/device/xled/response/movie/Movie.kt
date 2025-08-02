package de.visualdigits.kotlin.twinkly.model.device.xled.response.movie


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class Movie(
    code: Int? = null,
    val id: Int? = null,
    val name: String? = null,
    @JsonProperty("unique_id") val uniqueId: String? = null,
    @JsonProperty("descriptor_type") val descriptorType: String? = null,
    @JsonProperty("leds_per_frame") val ledsPerFrame: Int? = null,
    @JsonProperty("frames_number") val framesNumber: Int? = null,
    val fps: Int? = null
) : JsonObject(code)
