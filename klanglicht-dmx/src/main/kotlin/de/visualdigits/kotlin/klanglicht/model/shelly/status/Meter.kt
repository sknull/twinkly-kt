package de.visualdigits.kotlin.klanglicht.model.shelly.status

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime


class Meter(
    val power: Double? = null,
    @JsonProperty("overpower") val overPower: String? = null, // different types for rgbw and others
    @JsonProperty("is_valid") var isValid: Boolean? = null,
    val timestamp: OffsetDateTime? = null,
    val counters: List<Double> = listOf(),
    val total: Int? = null
)
