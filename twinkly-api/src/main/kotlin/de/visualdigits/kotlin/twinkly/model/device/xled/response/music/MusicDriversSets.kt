package de.visualdigits.kotlin.twinkly.model.device.xled.response.music


import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class MusicDriversSets(
    code: Int? = null,
    val current: Int? = null,
    val count: Int? = null,
    val driversets: List<Driverset> = listOf()
): JsonObject(code)
