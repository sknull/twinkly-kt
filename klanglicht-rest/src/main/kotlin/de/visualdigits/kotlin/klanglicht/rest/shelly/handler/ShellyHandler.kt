package de.visualdigits.kotlin.klanglicht.rest.shelly.handler

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import de.visualdigits.kotlin.klanglicht.model.hybrid.HybridScene
import de.visualdigits.kotlin.klanglicht.model.shelly.ShellyDevice
import de.visualdigits.kotlin.klanglicht.model.shelly.client.ShellyClient
import de.visualdigits.kotlin.klanglicht.model.shelly.status.Status
import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.feign.LightmanagerClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ShellyHandler {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    var client: LightmanagerClient? = null

    @Autowired
    val configHolder: ConfigHolder? = null

    /**
     * Sets the given scene or index on the connected lightmanager air.
     *
     * @param sceneId
     * @param index
     */
    fun control(
        sceneId: Int,
        index: Int
    ) {
        if (sceneId > 0) {
            log.info("control sceneId=$sceneId")
            client?.controlScene(sceneId)
        }
        else if (index > 0) {
            log.info("control index=$index")
            client?.controlIndex(index)
        }
        else {
            throw IllegalStateException("Either parameter scene or index must be set")
        }
    }

    /**
     * Set hex colors.
     *
     * @param ids The list of ids.
     * @param hexColors The list of hex colors.
     * @param gains The list of gains (taken from stage setup if omitted).
     * @param transitionDuration The fade duration in milli seconds.
     * @param turnOn Determines if the device should be turned on.
     */
    fun hexColors(
        ids: String,
        hexColors: String,
        gains: String,
        transitionDuration: Long,
        turnOn: Boolean,
        store: Boolean = true
    ) {
        val nextScene = HybridScene(ids, hexColors, gains, turnOn.toString(), preferences = configHolder?.preferences)

        configHolder?.currentScene?.fade(nextScene, transitionDuration, configHolder.preferences!!)

        if (store) {
            configHolder?.updateScene(nextScene)
        }
    }

    fun color(
        ids: String,
        red: Int,
        green: Int,
        blue: Int,
        gains: String,
        transitionDuration: Long,
        turnOn: Boolean,
        store: Boolean
    ) {
        val nextScene = HybridScene(ids, RGBColor(red, green, blue).hex(), gains, turnOn.toString(), preferences = configHolder?.preferences)

        configHolder?.currentScene?.fade(nextScene, transitionDuration, configHolder.preferences!!)

        if (store) {
            configHolder?.updateScene(nextScene)
        }
    }

    fun restoreColors(
        ids: String,
        transitionDuration: Long
    ) {
        val lIds = ids
            .split(",")
            .filter { it.trim().isNotEmpty() }
            .map { it.trim() }
        lIds.forEach { id ->
            configHolder!!.getFadeable(id)?.write(configHolder.preferences!!, transitionDuration = transitionDuration)
        }
    }

    fun power(
        ids: String,
        turnOn: Boolean,
        transitionDuration: Long
    ) {
        val lIds = ids
            .split(",")
            .filter { it.trim().isNotEmpty() }
            .map { it.trim() }
        lIds.forEach { id ->
        val sid = id.trim()
            val shellyDevice = configHolder!!.shellyDevices[sid]
            if (shellyDevice != null) {
                val ipAddress: String = shellyDevice.ipAddress
                val command: String = shellyDevice.command
                val lastColor = configHolder.getFadeable(sid)
                lastColor?.setTurnOn(turnOn)
                try {
                    ShellyClient.setPower(
                        ipAddress = ipAddress,
                        command = command,
                        turnOn = turnOn,
                        transitionDuration = transitionDuration
                    )
                } catch (e: Exception) {
                    log.warn("Could not set power for shelly devica at '$ipAddress'")
                }
            }
        }
    }

    fun gain(
        ids: String,
        gain: Int,
        transitionDuration: Long
    ) {
        val lIds = ids
            .split(",")
            .filter { it.trim().isNotEmpty() }
            .map { it.trim() }
        lIds.forEach { id ->
            val sid = id.trim()
            val shellyDevice = configHolder!!.shellyDevices[sid]
            if (shellyDevice != null) {
                val ipAddress: String = shellyDevice.ipAddress
                val lastColor = configHolder.getFadeable(sid)
                lastColor?.setGain(gain.toFloat())
                try {
                    ShellyClient.setGain(ipAddress = ipAddress, gain = gain, transitionDuration = transitionDuration)
                } catch (e: Exception) {
                    log.warn("Could not get gain for shelly at '$ipAddress'")
                }
            }
        }
    }

    fun currentPowers(): Map<String, Status> {
        val powers: MutableMap<String, Status> = LinkedHashMap()
        status().forEach { (device: ShellyDevice, status: Status) ->
            powers[device.name] = status
        }
        return powers
    }

    fun status(): Map<ShellyDevice, Status> {
        val statusMap: MutableMap<ShellyDevice, Status> = LinkedHashMap<ShellyDevice, Status>()
        configHolder!!.preferences?.shelly?.forEach { device ->
            val ipAddress: String = device.ipAddress
            var status: Status
            try {
                status = ShellyClient.getStatus(ipAddress)
            } catch (e: Exception) {
                log.warn("Could not get ststus for shelly at '$ipAddress'")
                status = Status()
                status.mode = "offline"
            }
            statusMap[device] = status
        }
        return statusMap
    }
}
