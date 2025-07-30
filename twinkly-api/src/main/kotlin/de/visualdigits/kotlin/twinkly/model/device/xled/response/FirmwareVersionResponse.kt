package de.visualdigits.kotlin.twinkly.model.device.xled.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

@JsonIgnoreProperties("versionParts")
class FirmwareVersionResponse(
    code: Int? = null,
    val version: String? = null
) : JsonObject(code) {

    val versionParts: Version? = version?.let { v -> Version(v) }
}
