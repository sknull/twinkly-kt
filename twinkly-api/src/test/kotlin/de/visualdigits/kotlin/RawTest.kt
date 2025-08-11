package de.visualdigits.kotlin

import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.AuthToken
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.XledMatrixDevice
import de.visualdigits.kotlin.twinkly.model.device.xled.response.ResponseCode
import de.visualdigits.kotlin.util.post
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.Base64
import kotlin.collections.get
import kotlin.random.Random

class RawTest {

    val log = LoggerFactory.getLogger(javaClass)

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
