package de.visualdigits.kotlin.klanglicht.model.dmx

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.visualdigits.kotlin.klanglicht.model.fixture.Fixture


@JsonIgnoreProperties("fixture")
data class DmxDevice(
    val manufacturer: String = "",
    val model: String = "",
    val mode: String = "",
    val baseChannel: Int = 0,
    val gain: Float = 0.0f
) {
    var fixture: Fixture? = null
}
