package de.visualdigits.kotlin.twinkly.model.scene

import de.visualdigits.kotlin.twinkly.model.common.JsonBaseObject
import java.io.File


class Scene(
    val type: SceneType,
    val frameDelay: Long? = null
) : JsonBaseObject() {

    companion object {

        fun unmarshall(file: File): Scene = unmarshall<Scene>(file)

        fun unmarshall(json: String): Scene = unmarshall<Scene>(json)
    }
}
