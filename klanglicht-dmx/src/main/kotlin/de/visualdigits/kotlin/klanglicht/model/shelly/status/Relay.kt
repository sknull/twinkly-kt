package de.visualdigits.kotlin.klanglicht.model.shelly.status

import com.fasterxml.jackson.annotation.JsonProperty


class Relay(
    @JsonProperty("ison") val isOn: Boolean? = null,
    @JsonProperty("has_timer") val hasTimer: Boolean? = null,
    @JsonProperty("timer_started") val timerStarted: Int? = null,
    @JsonProperty("timer_duration") val timerDuration: Int? = null,
    @JsonProperty("timer_remaining") val timerRemaining: Int? = null,
    @JsonProperty("overpower") val overPower: Boolean? = null,
    @JsonProperty("overtemperature") val overTemperature: Boolean? = null,
    @JsonProperty("is_valid") val isValid: Boolean? = null,
    val source: String? = null
)
