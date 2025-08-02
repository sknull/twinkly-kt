package de.visualdigits.kotlin.twinkly.model.device.xled.response.music


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class CurrentMusicDriverSetResponse(
    @JsonProperty("driverset_id") val driversetId: Int? = null,
    code: Int? = null
): JsonObject(code)
