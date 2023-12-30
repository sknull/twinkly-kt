package de.visualdigits.kotlin.klanglicht.model.shelly

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import de.visualdigits.kotlin.klanglicht.model.shelly.client.ShellyClient
import de.visualdigits.kotlin.klanglicht.model.shelly.status.Light
import de.visualdigits.kotlin.klanglicht.model.shelly.status.Status


data class ShellyDevice(
    val name: String = "",
    val model: String = "",
    val command: String = "",
    val ipAddress: String = "",
    val gain: Float = 0.0f
) {

    override fun toString(): String {
        return "Shelly $name [$ipAddress] $model"
    }

    fun setPower(
        command: String,
        turnOn: Boolean,
        transitionDuration: Long
    ): String {
        return ShellyClient.setPower(
            ipAddress = ipAddress,
            command = command,
            turnOn = turnOn,
            transitionDuration = transitionDuration
        )
    }

    fun setGain(
        gain: Int,
        transitionDuration: Long
    ): String {
        return ShellyClient.setGain(
            ipAddress = ipAddress,
            gain = gain,
            transitionDuration = transitionDuration
        )
    }

    fun getStatus(): Status {
        return ShellyClient.getStatus(ipAddress)
    }

    fun setColor(
        rgbColor: RGBColor,
        gain: Float = 1.0f,
        transitionDuration: Long = 0,
        turnOn: Boolean = true,
    ): Light {
        return ShellyClient.setColor(
            ipAddress = ipAddress,
            rgbColor = rgbColor,
            gain = gain,
            transitionDuration = transitionDuration,
            turnOn = turnOn
        )
    }
}
