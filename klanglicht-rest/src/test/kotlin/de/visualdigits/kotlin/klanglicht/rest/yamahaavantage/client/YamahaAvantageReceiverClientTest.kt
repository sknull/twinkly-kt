package de.visualdigits.kotlin.klanglicht.rest.yamahaavantage.client

import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences
import de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled("for local testing only")
internal class YamahaAvantageReceiverClientTest {

    private val preferences = Preferences.load(
        klanglichtDir = File("../klanglicht-dmx/src/test/resources/.klanglicht"),
        preferencesFileName = System.getenv("preferencesFileName")?:"preferences_livingroom.json"
    )

    private val URL = preferences.serviceMap["receiver"]?.url?:""

    @Test
    fun testSet() {
        val client = YamahaAvantageReceiverClient(URL)
//        val responseCode = client.setSurroundProgram("Sci-Fi")
        val responseCode = client.setSurroundProgram("Standard")
        assertEquals(0, responseCode.responseCode)
    }

    @Test
    fun testInfo() {
        val client = YamahaAvantageReceiverClient(URL)
        val deviceInfo = client.deviceInfo()
        println(deviceInfo)
    }

    @Test
    fun testFeatures() {
        val client = YamahaAvantageReceiverClient(URL)
        val features = client.features()
        println(features)
    }

    @Test
    fun testSoundProgramList() {
        val client = YamahaAvantageReceiverClient(URL)
        val soundProgramList = client.soundProgramList()
        println(soundProgramList.soundProgramList.sorted().joinToString("\n"))
    }
}
