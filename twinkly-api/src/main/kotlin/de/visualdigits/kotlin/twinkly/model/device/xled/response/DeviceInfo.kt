package de.visualdigits.kotlin.twinkly.model.device.xled.response


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class DeviceInfo(
    code: Int? = null,
    @JsonProperty("product_name") val productName: String? = null,
    @JsonProperty("product_version") val productVersion: String? = null,
    @JsonProperty("hardware_version") val hardwareVersion: String? = null,
    @JsonProperty("bytes_per_led") val bytesPerLed: Int? = null,
    @JsonProperty("hw_id") val hwId: String? = null,
    @JsonProperty("flash_size") val flashSize: Int? = null,
    @JsonProperty("led_type") val ledType: Int? = null,
    @JsonProperty("product_code") val productCode: String? = null,
    @JsonProperty("fw_family") val fwFamily: String? = null,
    @JsonProperty("device_name") val deviceName: String? = null,
    val uptime: String? = null,
    val mac: String? = null,
    val uuid: String? = null,
    @JsonProperty("max_supported_led") val maxSupportedLed: Int? = null,
    @JsonProperty("number_of_led") val numberOfLed: Int? = null,
    @JsonProperty("led_profile") val ledProfile: String? = null,
    @JsonProperty("frame_rate") val frameRate: Double? = null,
    @JsonProperty("measured_frame_rate") val measuredFrameRate: Double? = null,
    @JsonProperty("movie_capacity") val movieCapacity: Int? = null,
    @JsonProperty("max_movies") val maxMovies: Int? = null,
    @JsonProperty("wire_type") val wireType: Int? = null,
    val copyright: String? = null,
    val diagnostics: List<Any> = listOf()
) : JsonObject(code)
