package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.deviceinfo


import com.fasterxml.jackson.annotation.JsonProperty

data class DeviceInfo(
    @JsonProperty("response_code") val responseCode: Int = 0,
    @JsonProperty("model_name") val modelName: String = "",
    val destination: String = "",
    @JsonProperty("device_id") val deviceId: String = "",
    @JsonProperty("system_id") val systemId: String = "",
    @JsonProperty("system_version") val systemVersion: Double = 0.0,
    @JsonProperty("api_version") val apiVersion: Double = 0.0,
    @JsonProperty("netmodule_generation") val netmoduleGeneration: Int = 0,
    @JsonProperty("netmodule_version") val netmoduleVersion: String = "",
    @JsonProperty("netmodule_checksum") val netmoduleChecksum: String = "",
    @JsonProperty("serial_number") val serialNumber: String = "",
    @JsonProperty("category_code") val categoryCode: Int = 0,
    @JsonProperty("operation_mode") val operationMode: String = "",
    @JsonProperty("update_error_code") val updateErrorCode: String = "",
    @JsonProperty("net_module_num") val netModuleNum: Int = 0,
    @JsonProperty("update_data_type") val updateDataType: Int = 0,
    @JsonProperty("analytics_info") val analyticsInfo: AnalyticsInfo = AnalyticsInfo()
)
