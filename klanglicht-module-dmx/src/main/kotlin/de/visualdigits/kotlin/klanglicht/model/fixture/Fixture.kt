package de.visualdigits.kotlin.klanglicht.model.fixture

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import de.visualdigits.kotlin.klanglicht.model.parameter.IntParameter
import de.visualdigits.kotlin.klanglicht.model.parameter.ParameterSet
import java.io.File
import java.nio.file.Paths
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round


@JsonIgnoreProperties("channelsForCurrentMode")
data class Fixture(
    val calibration: Calibration = Calibration(),
    val channels: Map<String, List<Channel>> = mapOf(),
    val colorPresets: Map<String, Map<String, Int>> = mapOf(),
    val manufacturer: String? = null,
    val model: String? = null,
    val parameters: Map<String, Any>? = null
) {

    var channelsForCurrentMode: Map<String, Channel> = mapOf()

    companion object {
        fun load(klanglichtDir: File, fixtureName: String): Fixture {
            val fn = if (fixtureName.endsWith(".json")) fixtureName else "$fixtureName.json"
            return jacksonMapperBuilder().build().readValue(
                Paths.get(klanglichtDir.canonicalPath, "fixtures", fn).toFile(),
                Fixture::class.java
            )
        }
    }

    /**
     * Returns the list of channels for the given mode (if any).
     */
    fun channelsForMode(mode: String): List<Channel> = channels[mode] ?: listOf()


    /**
     * Returns whether the fixture has a pano parameter or not.
     */
    fun hasPano(): Boolean = channelsForCurrentMode.containsKey("Pan")

    /**
     * Returns the maximum panning angle in degrees.
     */
    fun maxPano(): Double = parameters?.get("maxPano")?.let { (it as Int).toDouble() }?:0.0

    /**
     * Returns the maximum value for the pano parameter according to the number of bytes supported within the current mode.
     */
    fun maxPanoValue(): Int = if (hasPano()) if (channelsForCurrentMode.containsKey("PanFine")) 65535 else 255 else 0

    /**
     * Returns a parameter set to be used to set the pano according to the given angle
     */
    fun panoParameterSet(angle: Double): ParameterSet {
        val maxPano = maxPano()
        val panoValue = round(maxPanoValue().toDouble() / maxPano * max(min(angle, maxPano), 0.0)).toInt()
        return ParameterSet(
            parameters = mutableListOf(
                IntParameter("Pan", (panoValue and 0xff00) shr 8),
                IntParameter("PanFine", panoValue and 0xff)
            )
        )
    }


    /**
     * Returns whether the fixture has a tilt parameter or not.
     */
    fun hasTilt(): Boolean = channelsForCurrentMode.containsKey("Tilt")

    /**
     * Returns the maximum tilting angle in degrees.
     */
    fun maxTilt(): Double = parameters?.get("maxTilt")?.let { (it as Int).toDouble() }?:0.0

    /**
     * Returns the maximum value for the tilt parameter according to the number of bytes supported within the current mode.
     */
    fun maxTiltValue(): Int = if (hasTilt()) if (channelsForCurrentMode.containsKey("TiltFine")) 65535 else 255 else 0

    /**
     * Returns a parameter set to be used to set the tilt according to the given angle
     */
    fun tiltParameterSet(angle: Double): ParameterSet {
        val maxTilt = maxTilt()
        val tiltValue = round(maxTiltValue().toDouble() / maxTilt * max(min(angle, maxTilt), 0.0)).toInt()
        return ParameterSet(
            parameters = mutableListOf(
                IntParameter("Tilt", (tiltValue and 0xff00) shr 8),
                IntParameter("TiltFine", tiltValue and 0xff)
            )
        )
    }
}
