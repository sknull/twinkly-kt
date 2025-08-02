package de.visualdigits.kotlin.twinkly.model.device.xled.response.movie


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.Sync
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.Mic

class LedMovieConfigResponse(
    code: Int? = null,
    @JsonProperty("frame_delay") val frameDelay: Int? = null,
    @JsonProperty("leds_number") val ledsNumber: Int? = null,
    @JsonProperty("\"loop_type\"") val loopType: Int? = null,
    @JsonProperty("frames_number") val framesNumber: Int? = null,
    val sync: Sync? = null,
    val mic: Mic? = null
) : JsonObject(code)
