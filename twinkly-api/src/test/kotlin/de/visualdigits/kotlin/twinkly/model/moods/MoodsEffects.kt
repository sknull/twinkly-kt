package de.visualdigits.kotlin.twinkly.model.moods

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties("effectsMap")
data class MoodsEffects(
    val effectssets: List<Effectsset> = listOf()
) {
    val effectsMap: Map<String, Effectsset> = effectssets.map { es -> Pair(es.uuid!!, es) }.toMap()
}
