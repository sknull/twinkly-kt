package de.visualdigits.kotlin.twinkly.model.xmusic.response.musicmode


import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MusicMode(
    val mode: String? = null,
    val config: Config? = null,
    code: Int? = null
) : JsonObject(code)
