package de.visualdigits.kotlin.klanglicht.rest.configuration

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import de.visualdigits.kotlin.klanglicht.model.dmx.DmxDevice
import de.visualdigits.kotlin.klanglicht.model.dmx.DmxInterface
import de.visualdigits.kotlin.klanglicht.model.dmx.DmxInterfaceType
import de.visualdigits.kotlin.klanglicht.model.dmx.DmxRepeater
import de.visualdigits.kotlin.klanglicht.model.hybrid.HybridDeviceType
import de.visualdigits.kotlin.klanglicht.model.hybrid.HybridScene
import de.visualdigits.kotlin.klanglicht.model.parameter.Fadeable
import de.visualdigits.kotlin.klanglicht.model.parameter.IntParameter
import de.visualdigits.kotlin.klanglicht.model.parameter.ParameterSet
import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences
import de.visualdigits.kotlin.klanglicht.model.shelly.ShellyColor
import de.visualdigits.kotlin.klanglicht.model.shelly.ShellyDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.DeviceOrigin
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledArray
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.apache.commons.lang3.SystemUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Paths
import kotlin.math.roundToInt

@Component
class ConfigHolder {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    var preferences: Preferences? = null
    var dmxInterface: DmxInterface? = null
    var repeater: DmxRepeater? = null

    val klanglichtDirectory: File = File(SystemUtils.getUserHome(), ".klanglicht")

    val currentScene: HybridScene = HybridScene()

    var shellyDevices: Map<String, ShellyDevice> = mapOf()

    var xledArray: XledArray? = null
    var xledDevices: Map<String, XLedDevice> = mapOf()

    @PostConstruct
    fun initialize() {
        // load preferences
        log.info("#### setUp - start")
        preferences = Preferences.load(klanglichtDirectory)
        val dmxPort = preferences?.dmx?.port!!

        // initialize dmx fixtures
        log.info("##")
        log.info("## klanglichtDirectory: " + klanglichtDirectory.absolutePath)
        log.info("## dmxPort            : $dmxPort")
        dmxInterface = preferences?.dmx?.interfaceType?.let {
            log.info("## Using DMX interface of type $it")
            DmxInterface.load(it)
        }
        dmxInterface?.open(dmxPort)
        if (dmxInterface?.isOpen() == true) {
            dmxInterface?.clear()
            if (preferences?.dmx?.enableRepeater  == true) {
                repeater = DmxRepeater.instance(dmxInterface!!)
                Thread.sleep(10)
                repeater?.play()
            }
        } else {
            log.warn("Could not open serial interface - falling back to dummy interface")
            dmxInterface = DmxInterface.load(DmxInterfaceType.Dummy)
        }

        // initialize shellies
        shellyDevices = preferences?.shelly?.associateBy { it.name }?: mapOf()

        // initialize twinkly devices
        val twinkly = preferences?.twinkly
        val deviceOrigin = twinkly?.deviceOrigin?.let { DeviceOrigin.valueOf(it) }?: DeviceOrigin.TOP_LEFT
        val xledDevices: MutableMap<String, XLedDevice> = mutableMapOf()
        xledArray = twinkly?.array?.map { column ->
            column.map { config ->
                val xledDevice = XLedDevice(host = config.ipAddress, config.width, config.height)
                xledDevices[config.name] = xledDevice
                xledDevice
            }.toTypedArray()
        }?.toTypedArray()
            ?.let { devices ->
                XledArray(deviceOrigin = deviceOrigin, xLedDevices = devices)
            }
        this.xledDevices = xledDevices
        log.info("##### Using twinkly devices '${xledDevices.keys}'")

        // build initial scene
        preferences?.stage?.forEach { hybridDevice ->
            when (hybridDevice.type) {
                HybridDeviceType.dmx -> {
                    val dmxDevice = preferences?.dmx?.dmxDevices?.get(hybridDevice.id)
                    if (dmxDevice != null) {
                        putFadeable(hybridDevice.id, ParameterSet(
                            baseChannel = dmxDevice.baseChannel,
                            parameters = mutableListOf(
                                IntParameter("MasterDimmer", (255 * dmxDevice.gain).roundToInt()),
                                RGBColor()
                            )
                        ))
                    }
                }
                HybridDeviceType.shelly -> {
                    val shellyDevice = preferences?.shellyMap?.get(hybridDevice.id)
                    if (shellyDevice != null) {
                        putFadeable(hybridDevice.id, ShellyColor(
                            deviceId = shellyDevice.name,
                            ipAddress = shellyDevice.ipAddress,
                            color = RGBColor(),
                            deviceGain = shellyDevice.gain,
                            deviceTurnOn = false
                        ))
                    }
                }
                else -> {}
            }
        }

        log.info("#### setUp - end")
    }

    @PreDestroy
    fun tearDown() {
        log.info("#### tearDown - start")
        if (dmxInterface?.isOpen() == true) {
            repeater?.end()
            Thread.sleep(10)
            dmxInterface?.clear()
            dmxInterface?.close()
        }
        log.info("#### tearDown - end")
    }

    fun getAbsoluteResource(relativeResourePath: String): File {
        return Paths.get(klanglichtDirectory.absolutePath, "resources", relativeResourePath).toFile()
    }

    fun getShellyGain(id: String): Float {
        return shellyDevices[id]?.gain?:1.0f
    }

    fun getShellyDevice(id: String): ShellyDevice? {
        return preferences?.shellyMap?.get(id)
    }

    fun getDmxDevice(id: String): DmxDevice? {
        return preferences?.dmx?.dmxDevices?.get(id)
    }

    fun putFadeable(id: String, fadeable: Fadeable<*>) {
        currentScene.putFadeable(id, fadeable)
    }

    fun getFadeable(id: String): Fadeable<*>? {
        return currentScene.getFadeable(id)
    }

    fun updateScene(nextScene: HybridScene) {
        currentScene.update(nextScene)
    }
}
