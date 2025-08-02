package de.visualdigits.kotlin.twinkly.model.device.xmusic.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MusicStatsResponse(
    code: Int? = null,
    val bpm: Int? = null,
    @JsonProperty("ag_b_band_total") val agBBandTotal: Int? = null,
    val micerr: Int? = null,
    val micvar: Double? = null
) : JsonObject(code)
