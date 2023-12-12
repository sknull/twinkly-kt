package de.visualdigits.kotlin.twinkly.model.device.xled.request

import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import java.util.*

class NewMovieRequest(
    val name: String? = null,
    @JsonProperty("unique_id") val uniqueId: String? = UUID.randomUUID().toString().uppercase(),
    @JsonProperty("descriptor_type") val descriptorType: String? = null,
    @JsonProperty("leds_per_frame") val ledsPerFrame: Int? = null,
    @JsonProperty("frames_number") val framesNumber: Int? = null,
    val fps: Int? = null
) : JsonObject()
