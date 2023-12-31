package de.visualdigits.kotlin.klanglicht.rest.configuration

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import java.io.File
import java.nio.file.Paths


@JsonIgnoreProperties("klanglichtDir", "dmxInterface", "fixtures", "serviceMap", "shellyMap", "stageMap")
data class Preferences(
    val name: String = "",
    val theme: String = "",
    val twinkly: TwinklyConfiguration? = null,
) {

    companion object {

        private val mapper = jacksonMapperBuilder().build()

        var preferences: Preferences? = null

        fun load(
            klanglichtDir: File,
            preferencesFileName: String = "preferences.json"
        ): Preferences {
            if (preferences == null) {
                preferences = mapper.readValue(
                    Paths.get(klanglichtDir.canonicalPath, "preferences", preferencesFileName).toFile(),
                    Preferences::class.java
                )
            }

            return preferences!!
        }
    }
}
