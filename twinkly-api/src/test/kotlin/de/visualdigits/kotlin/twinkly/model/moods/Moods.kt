package de.visualdigits.kotlin.twinkly.model.moods


data class Moods(
    val moods: MutableList<MoodsItem> = mutableListOf()
): MutableList<MoodsItem> by moods
