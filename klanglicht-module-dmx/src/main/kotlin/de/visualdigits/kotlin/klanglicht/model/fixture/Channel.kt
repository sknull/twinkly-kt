package de.visualdigits.kotlin.klanglicht.model.fixture


data class Channel(
    val channelOffset: Int = 0,
    val function: String? = null,
    val maxValue: Int = 255,
    val minValue: Int = 0,
    val name: String? = null,
    val ranges: List<Range>? = null,
    val type: ChannelType? = null
)
