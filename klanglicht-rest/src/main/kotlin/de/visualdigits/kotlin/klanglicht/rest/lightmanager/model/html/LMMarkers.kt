package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html

import org.apache.commons.lang3.StringUtils

class LMMarkers(
    var name: String? = null,
    var markers: MutableMap<Int, LMMarker> = mutableMapOf()
) {
    fun add(marker: LMMarker) {
        val attributes = LMNamedAttributes(marker.name, "separate", "actorId", "state")
        if (attributes.matched()) {
            val name = attributes.name
            if (StringUtils.isNotEmpty(name)) {
                marker.name = name
            }
            marker.separate = attributes.getOrDefault("separate", "false").toBoolean()
            marker.actorId = attributes["actorId"]
            marker.markerState = attributes["state"]
        }
        markers[marker.id!!] = marker
    }

    operator fun get(id: Int): LMMarker? {
        return markers[id]
    }

    fun getByActorId(aid: Int): Set<LMMarker> {
        val markers: MutableList<LMMarker> = mutableListOf()
        val said = aid.toString()
        for (m in this.markers.values) {
            if (m.actorId.equals(said)) {
                markers.add(m)
            }
        }
        return markers.toSet()
    }
}
