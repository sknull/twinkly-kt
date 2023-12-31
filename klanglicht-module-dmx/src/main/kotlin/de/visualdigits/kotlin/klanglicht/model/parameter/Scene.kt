package de.visualdigits.kotlin.klanglicht.model.parameter

import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences
import java.io.File

class Scene(
    val name: String,
    val parameterSet: List<ParameterSet>
) : Fadeable<Scene> {

    companion object {
        private val mapper = jacksonMapperBuilder().build()

        fun load(sceneFile: File): Scene {
            return mapper.readValue(sceneFile, Scene::class.java)
        }
    }

    /**
     * Writes the given scene into the internal dmx frame of the interface.
     */
    override fun write(preferences: Preferences, write: Boolean, transitionDuration: Long) {
        // first collect all frame data for the dmx frame to avoid lots of costly write operations to a serial interface
        parameterSet
            .sortedBy { it.baseChannel }
            .forEach { parameterSet ->
                val baseChannel = parameterSet.baseChannel
                val bytes = (preferences.fixtures[baseChannel]?.map { channel ->
                    (parameterSet.parameterMap[channel.name] ?: 0).toByte()
                } ?: listOf()).toByteArray()
                preferences.dmxInterface?.dmxFrame?.set(baseChannel, bytes)
            }
        if (write) preferences.dmxInterface?.write()
    }

    override fun fade(
        other: Any,
        factor: Double
    ): Scene {
        return if (other is Scene) {
            Scene(
                name = "Frame $factor",
                parameterSet = parameterSet
                    .sortedBy { it.baseChannel }
                    .zip(other.parameterSet.sortedBy { it.baseChannel })
                    .map { (paramsFrom, paramsTo) ->
                        paramsFrom.fade(paramsTo, factor)
                    }
            )
        } else throw IllegalArgumentException("Cannot not fade another type")
    }
}
