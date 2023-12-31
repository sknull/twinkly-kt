package de.visualdigits.kotlin.twinkly.model.device.xled.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class CurrentMusicDriverSet(
    @JsonProperty("driverset_id") val driversetId: Int? = null,
    code: Int? = null
): JsonObject(code)
