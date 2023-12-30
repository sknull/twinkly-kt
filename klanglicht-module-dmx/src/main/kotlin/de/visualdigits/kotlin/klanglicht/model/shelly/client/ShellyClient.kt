package de.visualdigits.kotlin.klanglicht.model.shelly.client

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import de.visualdigits.kotlin.klanglicht.model.shelly.status.Light
import de.visualdigits.kotlin.klanglicht.model.shelly.status.Status
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

object ShellyClient {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun setPower(
        ipAddress: String,
        turnOn: Boolean,
        command: String = "",
        transitionDuration: Long = 1
    ): String {
        log.debug("setPower: $ipAddress = $turnOn")
        val url =
            "http://" + ipAddress + "/" + command + "?turn=" + (if (turnOn) "on" else "off") + "&transition=" + transitionDuration + "&"
        return URL(url).readText()
    }

    fun setGain(
        ipAddress: String,
        gain: Int,
        transitionDuration: Long
    ): String {
        log.debug("setGain: $ipAddress = $gain")
        return URL("http://$ipAddress/color/0?gain=$gain&transition=$transitionDuration&").readText()
    }

    fun getStatus(
        ipAddress: String
    ): Status {
        log.debug("getStatus: $ipAddress")
        val json = URL("http://$ipAddress/status").readText()
        return Status.load(json)
    }

    fun setColor(
        ipAddress: String,
        rgbColor: RGBColor,
        gain: Float,
        transitionDuration: Long = 1, // zero is interpreted as empty which leads to the default of 2000 millis
        turnOn: Boolean = true,
    ): Light {
        log.debug("setColor: $ipAddress = ${rgbColor.ansiColor()} [$gain]")
        val url = "http://$ipAddress/color/0?" +
                "turn=${if (turnOn) "on" else "off"}&" +
                "red=${rgbColor.red}&" +
                "green=${rgbColor.green}&" +
                "blue=${rgbColor.blue}&" +
                "white=0&" +
                "gain=${(100 * gain).toInt()}&" +
                "transition=" + transitionDuration + "&"
        val json = URL(
            url
        ).readText()
        return Light.load(json)
    }
}
