package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html.deserializer.BooleanArrayDeserializer
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html.deserializer.LMParamsInitializer
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html.deserializer.NumericBooleanDeserializer
import java.io.IOException
import java.time.LocalDateTime

@JsonDeserialize(converter = LMParamsInitializer::class)
class LMParams(
    @JsonProperty("auth enabled") @JsonDeserialize(using = NumericBooleanDeserializer::class) var authEnabled: Boolean? = null,
    var time: String? = null,
    var date: String? = null,
    @JsonIgnore var dateTime: LocalDateTime? = null,
    var weekday: String? = null,
    @JsonProperty("is dst") @JsonDeserialize(using = NumericBooleanDeserializer::class)var dst: Boolean? = null,
    @JsonProperty("marker state") @JsonDeserialize(using = BooleanArrayDeserializer::class) var markerState: BooleanArray = booleanArrayOf(),
    var ssid: String? = null,
    @JsonProperty("led off") @JsonDeserialize(using = NumericBooleanDeserializer::class) var ledOff: Boolean? = null,
    @JsonProperty("last update") var lastUpdate: String? = null,
    @JsonProperty("firmware ver") var firmwareVer: String? = null,
    @JsonProperty("mac addr") var macAddr: String? = null,
    @JsonDeserialize(using = NumericBooleanDeserializer::class) var busy: Boolean? = null,
    @JsonProperty("master ip") var masterIp: String? = null,
    var lon: Double? = null,
    var lat: Double? = null,
    @JsonProperty("mode 433") @JsonDeserialize(using = NumericBooleanDeserializer::class) var mode433: Boolean? = null,
    @JsonProperty("mode 868") @JsonDeserialize(using = NumericBooleanDeserializer::class) var mode868: Boolean? = null,
    @JsonDeserialize(using = NumericBooleanDeserializer::class) var mpfs: Boolean? = null
) {

    companion object {

        private val mapper = jacksonMapperBuilder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()

        fun load(json: String): LMParams {
            val mmParams: LMParams
            mmParams = try {
                mapper.readValue(json, LMParams::class.java)
            } catch (e: IOException) {
                throw IllegalStateException("Could not unmarshall file: $json", e)
            }
            return mmParams
        }
    }
}
