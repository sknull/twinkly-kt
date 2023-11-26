package de.visualdigits.kotlin.twinkly.model.xmusic.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MusicStats(
    val bpm: Int? = null,
    @JsonProperty("ag_b_band_total") val agBBandTotal: Int? = null,
    val micerr: Int? = null,
    val micvar: Double? = null,
    code: Int? = null
) : JsonObject(code)
