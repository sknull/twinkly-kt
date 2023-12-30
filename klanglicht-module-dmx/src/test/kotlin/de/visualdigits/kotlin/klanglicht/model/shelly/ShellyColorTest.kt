package de.visualdigits.kotlin.klanglicht.model.shelly

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences
import org.junit.jupiter.api.Test
import java.io.File

class ShellyColorTest {

    val preferences = Preferences.load(
        klanglichtDir = File(ClassLoader.getSystemResource(".klanglicht").toURI()),
        preferencesFileName = "preferences_livingroom_dummy.json"
    )

    @Test
     fun testFade() {
        val shellyDevice = preferences.shellyMap["Starwars"]
        if (shellyDevice != null) {
            val color1 = ShellyColor("foo", shellyDevice.ipAddress, RGBColor(255, 0, 0), 1.0f, true)
            val color2 = ShellyColor("bar", shellyDevice.ipAddress, RGBColor(0, 255, 0), 1.0f, true)
//            color2.write()

            for (i in 0 until 5) {
                color1.fade(color2, 2000, preferences)
                Thread.sleep(2000)
                color2.fade(color1, 2000, preferences)
                Thread.sleep(2000)
            }
        }
     }
}
