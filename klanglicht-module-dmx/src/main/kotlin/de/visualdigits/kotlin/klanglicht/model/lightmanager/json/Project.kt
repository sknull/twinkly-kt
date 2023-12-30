package de.visualdigits.kotlin.klanglicht.model.lightmanager.json

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.json.JsonMapper
import java.io.IOException
import java.io.InputStream

class Project(
    val settings: Settings? = null,
    val marker: Map<String, Marker>? = null,
    val devices: Map<String, Device>? = null,
    val scenes: List<Scene> = listOf(),
    val actuators: List<Actuator> = listOf()
) {

    @JsonIgnore val scenesMap: Map<Int, Scene> = mapOf()
    @JsonIgnore val actuatorsMap: Map<Int, Actuator> = mapOf()

    private fun determineScenes(scenes: List<Scene>, scenesMap: MutableMap<Int, Scene>) {
        for (scene in scenes) {
            val properties = scene.properties
            scenesMap[properties?.index!!] = scene
            determineScenes(scene.children, scenesMap)
        }
    }

    private fun determineActuators(
        actuators: List<Actuator>,
        actuatorsMap: MutableMap<Int, Actuator>,
        scenes: List<Scene>
    ) {
        for (actuator in actuators) {
            val properties = actuator.properties
            actuatorsMap[properties?.index!!] = actuator
            for (scene in scenes) {
                if (scene.containsActuator(actuator.properties.index!!)) {
                    actuator.usedByScenes.add(scene)
                }
            }
            determineActuators(actuator.children, actuatorsMap, scenes)
        }
    }

    companion object {
        val MAPPER: JsonMapper = JsonMapper()
        fun load(ins: InputStream?): Project {
            val project: Project
            try {
                project = MAPPER.readValue<Project>(ins, Project::class.java)
                val scenesMap = project.scenesMap
                project.determineScenes(project.scenes, scenesMap.toMutableMap())
                project.determineActuators(
                    project.actuators,
                    project.actuatorsMap.toMutableMap(),
                    ArrayList<Scene>(scenesMap.values)
                )
            } catch (e: IOException) {
                throw IllegalStateException("Could not load json file", e)
            }
            return project
        }
    }
}
