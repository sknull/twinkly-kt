package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class Netusb(
    @JsonProperty("func_list") val funcList: List<String> = listOf(),
    val preset: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.PresetX = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.PresetX(),
    @JsonProperty("recent_info") val recentInfo: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.RecentInfo = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.RecentInfo(),
    @JsonProperty("play_queue") val playQueue: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.PlayQueue = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.PlayQueue(),
    @JsonProperty("mc_playlist") val mcPlaylist: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.McPlaylist = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.McPlaylist(),
    @JsonProperty("net_radio_type") val netRadioType: String = "",
    val tidal: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Tidal = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Tidal(),
    val qobuz: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Qobuz = de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Qobuz()
)
