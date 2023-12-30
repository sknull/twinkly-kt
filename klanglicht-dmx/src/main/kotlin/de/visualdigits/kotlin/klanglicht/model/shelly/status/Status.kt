package de.visualdigits.kotlin.klanglicht.model.shelly.status

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.time.LocalTime
import java.time.OffsetDateTime


class Status(
    @JsonProperty("wifi_sta") var wifiState: WifiState? = null,
    var cloud: Cloud? = null,
    var mqtt: Mqtt? = null,
    var time: LocalTime? = null,
    var unixtime: OffsetDateTime? = null,
    var serial: Int? = null,
    @JsonProperty("has_update") var hasUpdate: Boolean? = null,
    var mac: String? = null,
    @JsonProperty("cfg_changed_cnt") var configChangedCount: Int? = null,
    @JsonProperty("actions_stats") var actionStats: ActionStats? = null,
    var mode: String? = null,
    var input: Int? = null,
    var lights: List<Light>? = null,
    var relays: List<Relay>? = null,
    var meters: List<Meter>? = null,
    var inputs: List<Input>? = null,
    @JsonProperty("ext_sensors") var sensors: Sensor? = null,
    @JsonProperty("ext_temperature") var externalTemperatures: ExternalTemperature? = null,
    @JsonProperty("ext_humidity") var humidities: Humidity? = null,
    var temperature: Double? = null,
    @JsonProperty("temperature_status") var temperatureStatus: String? = null,
    @JsonProperty("overtemperature") var overTemperature: Boolean? = null,
    @JsonProperty("tmp") var tmp: Temperature? = null,
    var update: Update? = null,
    @JsonProperty("ram_total") var ramTotal: Long? = null,
    @JsonProperty("ram_free") var ramFree: Long? = null,
    @JsonProperty("fs_size") var fileSystemSize: Long? = null,
    @JsonProperty("fs_free") var fileSystemFree: Long? = null,
    var voltage: Double? = null,
    var uptime: Long? = null
) {

    companion object {
        val MAPPER: JsonMapper = JsonMapper
            .builder()
            .disable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .addModule(JavaTimeModule())
            .build()

        fun load(json: String?): Status {
            return try {
                MAPPER.readValue<Status>(json, Status::class.java)
            } catch (e: JsonProcessingException) {
                throw IllegalStateException("Could not read JSON string", e)
            }
        }
    }
}
