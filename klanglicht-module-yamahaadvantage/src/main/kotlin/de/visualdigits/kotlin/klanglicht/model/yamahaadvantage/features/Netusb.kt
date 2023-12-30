package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features


import com.fasterxml.jackson.annotation.JsonProperty

data class Netusb(
    @JsonProperty("func_list") val funcList: List<String> = listOf(),
    val preset: PresetX = PresetX(),
    @JsonProperty("recent_info") val recentInfo: RecentInfo = RecentInfo(),
    @JsonProperty("play_queue") val playQueue: PlayQueue = PlayQueue(),
    @JsonProperty("mc_playlist") val mcPlaylist: McPlaylist = McPlaylist(),
    @JsonProperty("net_radio_type") val netRadioType: String = "",
    val tidal: Tidal = Tidal(),
    val qobuz: Qobuz = Qobuz()
)
