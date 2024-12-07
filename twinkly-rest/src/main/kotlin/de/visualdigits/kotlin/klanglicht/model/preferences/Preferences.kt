package de.visualdigits.kotlin.klanglicht.model.preferences

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.visualdigits.kotlin.klanglicht.model.twinkly.TwinklyConfiguration
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths


@JsonIgnoreProperties("klanglichtDir", "dmxInterface", "fixtures", "serviceMap", "shellyMap", "twinklyMap", "stageMap", "colorWheelMap", "log")
class Preferences(
    var name: String = "",
    var installationLat: Double = 0.0,
    var installationLon: Double = 0.0,
    var theme: String = "",
    var fadeDurationDefault: Long = 0,
    var baseUrl: String? = null,
    var twinkly: List<TwinklyConfiguration>? = listOf(),
) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private var twinklyMap: Map<String, TwinklyConfiguration> = mapOf()

    private var xledArrays: Map<String, XledArray> = mapOf()

    private var xledDevices: Map<String, XLedDevice> = mapOf()

    companion object {

        private val mapper = jacksonObjectMapper()

        var preferences: Preferences? = null

        fun load(
            klanglichtDirectory: File,
            preferencesFileName: String = "preferences.json"
        ): Preferences {
            if (preferences == null) {
                val json = Paths.get(klanglichtDirectory.canonicalPath, "preferences", preferencesFileName).toFile().readText()
                val prefs = mapper.readValue(json, Preferences::class.java)
                prefs.initialize(klanglichtDirectory)
                preferences = prefs
            }

            return preferences!!
        }
    }

    fun initialize(klanglichtDirectory: File) {
        twinklyMap = twinkly?.map { Pair(it.name, it) }?.toMap()?: mapOf()
        log.info("## Twinkly devices: ${twinklyMap.keys}")

        val xledDevices: MutableMap<String, XLedDevice> = mutableMapOf()
        xledArrays = twinkly?.associate { config ->
            Pair(config.name, config.xledArray)
        } ?: mapOf()
        this.xledDevices = xledDevices
    }

    fun getTwinklyConfiguration(id: String): TwinklyConfiguration? = twinklyMap[id]

    fun getXledDevice(id: String): XLedDevice? = xledDevices[id]

    fun getXledArray(id: String): XledArray? = xledArrays[id]

    fun getXledArrays(): List<XledArray> = xledArrays.values.toList()

    fun getDmxFrameTime(): Long = 50
}
