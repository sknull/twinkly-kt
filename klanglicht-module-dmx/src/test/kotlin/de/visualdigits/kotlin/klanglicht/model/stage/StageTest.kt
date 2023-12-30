package de.visualdigits.kotlin.klanglicht.model.stage

import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences
import org.junit.jupiter.api.Test
import java.io.File

class StageTest {

    private val prefs = Preferences.load(
        klanglichtDir = File(ClassLoader.getSystemResource(".klanglicht").toURI()),
        preferencesFileName = "preferences_livingroom.json"
    )

    @Test
    fun testStage() {
        prefs.dmx?.devices?.forEach { stageFixture ->
            val fixture = stageFixture.fixture!!
            println("${stageFixture.manufacturer}_${stageFixture.model}_${stageFixture.mode} [${stageFixture.baseChannel}] hasPano: ${fixture.hasPano()} [max value ${fixture.maxPanoValue()}] hasTilt: ${fixture.hasTilt()} [max value ${fixture.maxTiltValue()}]")
        }
    }
}
