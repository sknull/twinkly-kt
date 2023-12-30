package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class Zone(
    val id: String = "",
    @JsonProperty("func_list") val funcList: List<String> = listOf(),
    @JsonProperty("input_list") val inputList: List<String> = listOf(),
    @JsonProperty("sound_program_list") val soundProgramList: List<String>? = listOf(),
    @JsonProperty("surr_decoder_type_list") val surrDecoderTypeList: List<String>? = listOf(),
    @JsonProperty("tone_control_mode_list") val toneControlModeList: List<String> = listOf(),
    @JsonProperty("link_control_list") val linkControlList: List<String> = listOf(),
    @JsonProperty("link_audio_delay_list") val linkAudioDelayList: List<String>? = listOf(),
    @JsonProperty("range_step") val rangeStep: List<de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.RangeStep> = listOf(),
    @JsonProperty("scene_num") val sceneNum: Int = 0,
    @JsonProperty("cursor_list") val cursorList: List<String>? = listOf(),
    @JsonProperty("menu_list") val menuList: List<String>? = listOf(),
    @JsonProperty("actual_volume_mode_list") val actualVolumeModeList: List<String> = listOf(),
    @JsonProperty("ccs_supported") val ccsSupported: List<String>? = listOf(),
    @JsonProperty("zone_b") val zoneB: Boolean? = false
)
