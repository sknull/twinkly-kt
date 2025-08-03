package de.visualdigits.kotlin.twinkly.model.moods


data class MoodsEffectsNames(
    val effectNames: MutableMap<String, String> = mutableMapOf()
): MutableMap<String, String> by effectNames
