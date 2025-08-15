package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.color.RGBColor
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.AuthToken
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedArray
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.util.post
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.Base64
import kotlin.random.Random

class RawTest {

    val log = LoggerFactory.getLogger(javaClass)

    @Test
    fun testTest() {
        val xled = XLedDevice.instance(ipAddress = "192.168.178.52", name = "curtain2", width = 10, height = 21)
        xled.powerOff()
    }

    @Test
    fun testHarlekin() {
        val curtain1 = XLedDevice.instance(ipAddress = "192.168.178.38", name = "curtain1", width = 10, height = 21)
        val curtain2 = XLedDevice.instance(ipAddress = "192.168.178.52", name = "curtain2", width = 10, height = 21)
        val curtain3 = XLedDevice.instance(ipAddress = "192.168.178.58", name = "curtain3", width = 10, height = 21)
        val curtain4 = XLedDevice.instance(ipAddress = "192.168.178.60", name = "curtain4", width = 10, height = 21)

        val xledArray = XLedArray.instance(
            mutableListOf(
                mutableListOf(
                    curtain1,
                    curtain2,
                ),
                mutableListOf(
                    curtain3,
                    curtain4
                )
            )
        )

        val red = XledFrame(10, 21, RGBColor(255, 0, 0))
        val green = XledFrame(10, 21, RGBColor(0, 255, 0))
        val blue = XledFrame(10, 21, RGBColor(0, 0, 255))
        val cyan = XledFrame(10, 21, RGBColor(0, 255, 255))
        val yellow = XledFrame(20, 42, RGBColor(255, 255, 0))

//        red.play(curtain1)
        green.play(curtain2)
//        blue.play(curtain3)
//        cyan.play(curtain4)
//        yellow.play(xledArray)
    }

    @Test
    fun testConnect() {
        val ipAddress = "192.168.178.34"
        val baseUrl = "http://$ipAddress/xled/v1"

        val challenge = Base64.getEncoder().encode(Random(System.currentTimeMillis()).nextBytes(32)).decodeToString()

        val response = URL("$baseUrl/login").post(
            body = "{\"challenge\":\"$challenge\"}".toByteArray(),
            headers =  mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            clazz = Map::class.java
        )?:error("No response")
        val token = response["authentication_token"] as String
        val expireInSeconds = response["authentication_token_expires_in"] as Int
        val responseChallenge = response["challenge-response"] as String
        val responseVerify = URL("$baseUrl/verify").post(
            body = "{\"challenge_response\":\"${responseChallenge}\"}".toByteArray(),
            headers =  mutableMapOf(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            ),
            clazz = JsonObject::class.java
        )?:error("No verify response")
        if (responseVerify.responseCode != ResponseCode.Ok) {
            log.warn("Could not login to device at ip address '$ipAddress'")
        }
        val tokenExpires = System.currentTimeMillis() + expireInSeconds * 1000 - 5000
//        log.debug("#### Token expires '${formatEpoch(tokenExpires)}'")
        val authToken = AuthToken(token, tokenExpires, true)

        println(authToken)
    }
}
