package de.visualdigits.kotlin.twinkly.model.device.xled.response.mode

import de.visualdigits.kotlin.twinkly.model.common.JsonBaseObject

class EffectConfig(
    val colorTransactionSpeed: Int? = null,
    val ledsOn: Int? = null,
    val paletteId: Int? = null,
    val rotationDirection: Int? = null,
    val scrollingDirection: Int? = null,
    val stringsNo: Int? = null,
    val transactionSpeed: Int? = null,
) : JsonBaseObject()
