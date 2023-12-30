package de.visualdigits.kotlin.klanglicht.model.preferences

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import de.visualdigits.kotlin.klanglicht.model.dmx.Dmx
import de.visualdigits.kotlin.klanglicht.model.dmx.DmxDevice
import de.visualdigits.kotlin.klanglicht.model.dmx.DmxInterface
import de.visualdigits.kotlin.klanglicht.model.dmx.DmxInterfaceDummy
import de.visualdigits.kotlin.klanglicht.model.dmx.DmxInterfaceType
import de.visualdigits.kotlin.klanglicht.model.fixture.Channel
import de.visualdigits.kotlin.klanglicht.model.fixture.Fixtures
import de.visualdigits.kotlin.klanglicht.model.hybrid.HybridDevice
import de.visualdigits.kotlin.klanglicht.model.shelly.ShellyDevice
import de.visualdigits.kotlin.klanglicht.model.twinkly.TwinklyConfiguration
import java.io.File
import java.nio.file.Paths


@JsonIgnoreProperties("klanglichtDir", "dmxInterface", "fixtures", "serviceMap", "shellyMap", "stageMap")
data class Preferences(
    val name: String = "",
    val theme: String = "",
    val services: List<Service> = listOf(),
    val shelly: List<ShellyDevice>? = listOf(),
    val twinkly: TwinklyConfiguration? = null,
    val stage: List<HybridDevice> = listOf(),
    val dmx: Dmx? = null
) {

    var klanglichtDir: File = File(".")

    var dmxInterface: DmxInterface = DmxInterfaceDummy()

    /** contains the list of channels for a given base dmx channel. */
    var fixtures: Map<Int, List<Channel>> = mapOf()

    var serviceMap: Map<String, Service> = mapOf()

    var shellyMap: Map<String, ShellyDevice> = mapOf()

    var stageMap: Map<String, HybridDevice> = mapOf()

    companion object {

        private val mapper = jacksonMapperBuilder().build()

        var preferences: Preferences? = null

        fun load(
            klanglichtDir: File,
            preferencesFileName: String = "preferences.json"
        ): Preferences {
            if (preferences == null) {
                val prefs = mapper.readValue(
                    Paths.get(klanglichtDir.canonicalPath, "preferences", preferencesFileName).toFile(),
                    Preferences::class.java
                )
                prefs.load(klanglichtDir)
                preferences = prefs
            }

            return preferences!!
        }
    }

    fun getDmxFrameTime(): Long = dmx?.frameTime?:50

    fun getDmxDevice(id: String): DmxDevice? = dmx?.dmxDevices?.get(id)

    fun load(klanglichtDir: File) {
        this.klanglichtDir = klanglichtDir

        val dmxFixtures = Fixtures.load(klanglichtDir)
        fixtures = dmx?.devices?.mapNotNull { stageFixture ->
            dmxFixtures.getFixture(stageFixture.manufacturer, stageFixture.model)
                ?.let { fixture ->
                    stageFixture.fixture = fixture
                    fixture.channelsForMode(stageFixture.mode).let { channels -> Pair(stageFixture.baseChannel, channels) }?:null
                }
        }?.toMap()
            ?:mapOf()

        serviceMap = services.associateBy { it.name }

        stageMap = stage.associateBy { it.id }

        shellyMap = shelly?.associateBy { it.name }?:mapOf()

        dmxInterface = dmx?.interfaceType?.let { DmxInterface.load(it) }?:DmxInterface.load(DmxInterfaceType.Dummy)
        dmx?.port?.let { dmxInterface.open(it) }
    }
}
