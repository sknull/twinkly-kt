package de.visualdigits.kotlin.twinkly.model.common.networkstatus


import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class NetworkStatus(
    val mode: Int? = null,
    val station: Station = Station(),
    val ap: Ap = Ap(),
    code: Int? = null
) : JsonObject(code)
