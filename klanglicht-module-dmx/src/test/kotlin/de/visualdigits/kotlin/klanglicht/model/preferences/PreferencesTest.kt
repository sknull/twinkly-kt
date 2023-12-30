package de.visualdigits.kotlin.klanglicht.model.preferences

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.File

internal class PreferencesTest {

    @Test
    fun testReadModel() {
        val preferences = Preferences.load(
            klanglichtDir = File(ClassLoader.getSystemResource(".klanglicht").toURI()),
            preferencesFileName = "preferences_livingroom.json"
        )

        assertNotNull(Preferences.preferences)
    }
}
