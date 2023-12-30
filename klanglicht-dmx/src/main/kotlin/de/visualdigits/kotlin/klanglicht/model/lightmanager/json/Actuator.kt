package de.visualdigits.kotlin.klanglicht.model.lightmanager.json

import com.fasterxml.jackson.annotation.JsonIgnore

class Actuator(
    val nodeindex: Int? = null,
    val expanded: Boolean? = null,
    val properties: ActuatorProperties? = null,
    val children: List<Actuator> = listOf()
) {
    @JsonIgnore
    val usedByScenes: MutableList<Scene> = mutableListOf()
}
