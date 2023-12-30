package de.visualdigits.kotlin.klanglicht.model.shelly.status

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder


class Light(
    @JsonProperty("ison") val isOn: Boolean? = null,
    val source: String? = null,
    @JsonProperty("has_timer") val hasTimer: Boolean? = null,
    @JsonProperty("timer_started") val timerStarted: Int? = null,
    @JsonProperty("timer_duration") val timerDuration: Int? = null,
    @JsonProperty("timer_remaining") val timerRemaining: Int? = null,
    val mode: String? = null,
    val red: Int? = null,
    val green: Int? = null,
    val blue: Int? = null,
    val white: Int? = null,
    val gain: Int? = null, // 0 - 100
    val effect: Int? = null,
    val transiton: Int? = null, // 0 - 5000
    val power: Double? = null,
    @JsonProperty("overpower") val overPower: Boolean? = null
) {
    companion object {

        private val mapper = jacksonMapperBuilder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()

        fun load(json: String?): Light {
            return try {
                mapper.readValue(json, Light::class.java)
            } catch (e: JsonProcessingException) {
                throw IllegalStateException("Could not read JSON string", e)
            }
        }
    }
}
