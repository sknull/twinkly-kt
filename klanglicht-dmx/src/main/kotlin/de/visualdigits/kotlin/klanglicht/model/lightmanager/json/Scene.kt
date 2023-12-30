package de.visualdigits.kotlin.klanglicht.model.lightmanager.json


class Scene(
    val nodeindex: Int? = null,
    val expanded: Boolean? = null,
    val properties: SceneProperties? = null,
    val children: List<Scene> = listOf()
) {
    fun containsActuator(actuatorIndex: Int): Boolean {
        for (child in children!!) {
            if (child.properties?.actorIndex == actuatorIndex) {
                return true
            }
            else {
                for (childChild in child.children) {
                    if (childChild.containsActuator(actuatorIndex)) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
