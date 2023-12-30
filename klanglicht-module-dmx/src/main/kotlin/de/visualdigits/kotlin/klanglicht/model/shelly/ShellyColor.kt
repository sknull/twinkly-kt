package de.visualdigits.kotlin.klanglicht.model.shelly

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import de.visualdigits.kotlin.klanglicht.model.parameter.Fadeable
import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences
import de.visualdigits.kotlin.klanglicht.model.shelly.client.ShellyClient

class ShellyColor(
    private val deviceId: String,
    val ipAddress: String,
    private var color: RGBColor,
    private var deviceGain: Float,
    private var deviceTurnOn: Boolean
) : Fadeable<ShellyColor> {

    override fun getTurnOn(): Boolean = deviceTurnOn

    override fun setTurnOn(turnOn: Boolean) {
        this.deviceTurnOn = turnOn
    }

    override fun getId(): String = deviceId

    override fun getGain(): Float = deviceGain

    override fun setGain(gain: Float) {
        this.deviceGain = gain
    }

    override fun getRgbColor(): RGBColor = color.clone()

    override fun setRgbColor(rgbColor: RGBColor) {
        color = rgbColor.clone()
    }

    override fun write(preferences: Preferences, write: Boolean, transitionDuration: Long) {
        ShellyClient.setColor(
            ipAddress = ipAddress,
            rgbColor = color,
            gain = deviceGain,
            transitionDuration = transitionDuration,
            turnOn = deviceTurnOn
        )
    }

    override fun fade(other: Any, factor: Double): ShellyColor {
        return if (other is ShellyColor) {
            ShellyColor(deviceId, ipAddress, color.fade(other.color, factor), deviceGain, deviceTurnOn)
        } else {
            throw IllegalArgumentException("Cannot not fade another type")
        }
    }
}
