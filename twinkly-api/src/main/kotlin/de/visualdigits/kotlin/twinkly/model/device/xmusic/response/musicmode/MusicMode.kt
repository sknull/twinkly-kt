package de.visualdigits.kotlin.twinkly.model.device.xmusic.response.musicmode


import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MusicMode(
    code: Int? = null,
    val mode: String? = null,
    val config: Config? = null
) : JsonObject(code)
