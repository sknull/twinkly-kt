package de.visualdigits.kotlin.klanglicht.model.hybrid

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import de.visualdigits.kotlin.klanglicht.model.parameter.Fadeable
import de.visualdigits.kotlin.klanglicht.model.parameter.IntParameter
import de.visualdigits.kotlin.klanglicht.model.parameter.ParameterSet
import de.visualdigits.kotlin.klanglicht.model.parameter.Scene
import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences
import de.visualdigits.kotlin.klanglicht.model.shelly.ShellyColor
import de.visualdigits.kotlin.klanglicht.model.shelly.client.ShellyClient
import kotlin.math.min
import kotlin.math.roundToInt

class HybridScene() : Fadeable<HybridScene> {

    private var ids: String = ""
    private var hexColors: String = ""
    private var gains: String = ""
    private var turnOns: String = ""
    private var preferences: Preferences? = null

    private val fadeables: MutableMap<String, Fadeable<*>> = mutableMapOf()

    constructor(
        ids: String,
        hexColors: String,
        gains: String,
        turnOns: String = "true",
        preferences: Preferences?
    ): this() {
        this.ids = ids
        this.hexColors = hexColors
        this.gains = gains
        this.turnOns = turnOns
        this.preferences = preferences

        initializeFromParameters()
        initializeFromFadeables() // modify attributes after updating fadeables
    }

    constructor(
        fadeables: MutableMap<String, Fadeable<*>>,
        preferences: Preferences?
    ): this() {
        this.fadeables.clear()
        this.preferences = preferences
        update(fadeables)
    }

    fun update(nextScene: HybridScene) {
        update(nextScene.fadeables)
    }

    fun update(fadeables: MutableMap<String, Fadeable<*>>) {
        this.fadeables.putAll(fadeables)
        initializeFromFadeables()
    }

    fun fadeables(): List<Fadeable<*>> = fadeables.values.toList()

    fun getFadeable(id: String): Fadeable<*>? = fadeables[id]

    fun putFadeable(id: String, fadeable: Fadeable<*>) {
        fadeables[id] = fadeable
        initializeFromFadeables()
    }

    private fun initializeFromFadeables() {
        this.ids = this.fadeables().map { sc -> sc.getId() }.joinToString(",")
        this.hexColors = this.fadeables().mapNotNull { sc -> sc.getRgbColor()?.hex() }.joinToString(",")
        this.gains = this.fadeables().map { sc -> sc.getGain() }.joinToString(",")
        this.turnOns = this.fadeables().mapNotNull { sc -> sc.getTurnOn() }.joinToString(",")
    }

    private fun initializeFromParameters() {
        val lIds = if (ids.isNotEmpty()) {
            ids
                .split(",")
                .filter { it.isNotEmpty() }
                .map { it.trim() }
        } else {
            preferences?.stage?.map { it.id }?:listOf()
        }

        val lHexColors = hexColors
            .split(",")
            .filter { it.isNotEmpty() }
        val nh = lHexColors.size - 1
        var h = 0

        val lGains = gains
            .split(",")
            .filter { it.isNotEmpty() }
            .map { it.toFloat() }
        val ng = lGains.size - 1
        var g = 0

        val lTurnOns = turnOns
            .split(",")
            .filter { it.isNotEmpty() }
            .map { it.toBoolean() }
        val nt = lTurnOns.size - 1
        var t= 0

        if (lIds.isNotEmpty()) {
            lIds.forEach { id ->
                val device = preferences?.stageMap?.get(id)
                if (device != null) {
                    val hexColor = lHexColors[min(nh, h++)]
                    val gain = lGains.getOrNull(min(ng, g++))
                    val turnOn = lTurnOns.getOrNull(min(nt, t++))?:false
                    processDevice(device, preferences, id, gain, turnOn, hexColor)
                        ?.let { dd -> fadeables[id] = dd }
                }
            }
        }
    }

    override fun toString(): String {
        return hexColors
            .split(",")
            .filter { it.isNotEmpty() }
            .map { RGBColor(it).ansiColor() }
            .joinToString("")
    }

    fun setTurnOn(id: String, turnOn: Boolean) {
        fadeables[id]?.setTurnOn(turnOn)
        initializeFromFadeables()
    }

    fun getTurnOn(id: String): Boolean? = fadeables[id]?.getTurnOn()

    fun setGain(id: String, gain: Float) {
        fadeables[id]?.setGain(gain)
        initializeFromFadeables()
    }

    fun getGain(id: String): Float = fadeables[id]?.getGain()?:1.0f

    fun setRgbColor(id: String, rgbColor: RGBColor) {
        fadeables[id]?.setRgbColor(rgbColor)
        initializeFromFadeables()
    }

    fun getRgbColor(id: String): RGBColor? = fadeables[id]?.getRgbColor()

    private fun processDevice(
        device: HybridDevice,
        preferences: Preferences?,
        id: String,
        gain: Float?,
        turnOn: Boolean,
        hexColor: String
    ): Fadeable<*>? {
        return when (device.type) {
            HybridDeviceType.dmx -> {
                val dmxDevice = preferences?.getDmxDevice(id)
                if (dmxDevice != null) {
                    val paramGain = if (turnOn) (255 * (gain ?: dmxDevice.gain)).roundToInt() else 0
                    ParameterSet(
                        baseChannel = dmxDevice.baseChannel,
                        parameters = mutableListOf(
                            IntParameter("MasterDimmer", paramGain),
                            RGBColor(hexColor)
                        )
                    )
                } else null
            }

            HybridDeviceType.shelly -> {
                val shellyDevice = preferences?.shellyMap?.get(id)
                if (shellyDevice != null) {
                    ShellyColor(
                        deviceId = shellyDevice.name,
                        ipAddress = shellyDevice.ipAddress,
                        color = RGBColor(hexColor),
                        deviceGain = gain ?: shellyDevice.gain,
                        deviceTurnOn = turnOn
                    )
                } else null
            }

            else -> null
        }
    }

    override fun write(preferences: Preferences, write: Boolean, transitionDuration: Long) {
        // first collect all frame data for the dmx frame to avoid lots of costly write operations to a serial interface
        fadeables().filterIsInstance<ParameterSet>().forEach { parameterSet ->
            val baseChannel = parameterSet.baseChannel
            val bytes = parameterSet.toBytes(preferences)
            preferences.dmxInterface?.dmxFrame?.set(baseChannel, bytes)
        }
        if (write) preferences.dmxInterface?.write()

        // call shelly interface which is pretty fast
        fadeables().filterIsInstance<ShellyColor>().forEach { it.write(preferences, true) }
    }

    override fun fade(other: Any, factor: Double): HybridScene {
        return if (other is HybridScene) {
            val otherIds = other.fadeables().map { it.getId() } // ensure that we only fade elements which are in source and target scene
            val fadedHexColors = fadeables()
                .filter { otherIds.contains(it.getId())  }
                .zip(other.fadeables())
                .mapNotNull {
                    when (val faded = it.first.fade(it.second, factor)) {
                        is ShellyColor -> {
                            Pair(faded.getId(), faded.getRgbColor())
                        }
                        is ParameterSet -> {
                            val second = faded.getRgbColor()
                            Pair(faded.getId(), second)
                        }
                        else -> null
                    }
                }.toMap()
            val fadedScene = HybridScene(fadeables, preferences)
            fadedHexColors.forEach { (id, hexColor) ->
                hexColor?.let { hc ->
                    fadedScene.setRgbColor(id, hc)
                }
            }

            // fixme - at this point parameterMap contains default values - why?

            fadedScene
        } else {
            throw IllegalArgumentException("Cannot not fade another type")
        }
    }
}
