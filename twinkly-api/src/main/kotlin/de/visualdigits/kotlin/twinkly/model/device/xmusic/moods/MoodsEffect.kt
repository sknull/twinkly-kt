package de.visualdigits.kotlin.twinkly.model.device.xmusic.moods

interface MoodsEffect {

    val index: Int

    val label: String

    fun moodIndex(): Int
}
