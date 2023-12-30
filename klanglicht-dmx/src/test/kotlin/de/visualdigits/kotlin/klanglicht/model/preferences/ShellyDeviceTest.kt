package de.visualdigits.kotlin.klanglicht.model.preferences

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import org.junit.jupiter.api.Test
import java.io.File

class ShellyDeviceTest {

    val preferences = Preferences.load(
        klanglichtDir = File(ClassLoader.getSystemResource(".klanglicht").toURI()),
        preferencesFileName = "preferences_livingroom_dummy.json"
    )

    @Test
    fun testSetColor() {
        preferences.shellyMap["Starwars"]?.setColor(RGBColor(0,0,255))
    }
}
