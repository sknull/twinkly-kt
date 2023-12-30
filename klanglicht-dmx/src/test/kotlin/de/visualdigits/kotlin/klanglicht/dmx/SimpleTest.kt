package de.visualdigits.kotlin.klanglicht.dmx

import de.visualdigits.kotlin.klanglicht.model.color.RGBWColor
import de.visualdigits.kotlin.klanglicht.model.parameter.IntParameter
import de.visualdigits.kotlin.klanglicht.model.parameter.ParameterSet
import de.visualdigits.kotlin.klanglicht.model.parameter.Scene
import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled("for local testing only")
class SimpleTest {

    val preferences = Preferences.load(
        klanglichtDir = File(ClassLoader.getSystemResource(".klanglicht").toURI()),
        preferencesFileName = "preferences_minimal.json"
    )

    @Test
    fun testRgbw() {
        val scene = Scene(
            name = "JUnit Test",
            parameterSet = listOf(
                ParameterSet(
                    baseChannel = 15,
                    parameters = mutableListOf(
                        IntParameter("MasterDimmer", 255),
                        RGBWColor(20, 20, 0, 0)
                    )
                ),
                ParameterSet(
                    baseChannel = 29,
                    parameters = mutableListOf(
                        IntParameter("MasterDimmer", 255),
                        RGBWColor(0, 20, 20, 0)
                    )
                )
            )
        )

        scene.write(preferences,)
    }

    @Test
    fun testPowerOff() {
        val scene0 = Scene(
            name = "JUnit Test",
            parameterSet = listOf(
                ParameterSet(
                    baseChannel = 15,
                    parameters = mutableListOf(
                        IntParameter("MasterDimmer", 0),
                        RGBWColor(0, 0, 0, 0)
                    )
                ),
                ParameterSet(
                    baseChannel = 29,
                    parameters = mutableListOf(
                        IntParameter("MasterDimmer", 0),
                        RGBWColor(0, 0, 0, 0)
                    )
                ),
            )
        )
        scene0.write(preferences,)
    }

    @Test
    fun testFade() {
        val scene0 = Scene(
            name = "JUnit Test",
            parameterSet = listOf(
                ParameterSet(
                    baseChannel = 15,
                    parameters = mutableListOf(
                        IntParameter("MasterDimmer", 0),
                        RGBWColor(0, 0, 0, 0)
                    )
                ),
                ParameterSet(
                    baseChannel = 29,
                    parameters = mutableListOf(
                        IntParameter("MasterDimmer", 0),
                        RGBWColor(0, 0, 0, 0)
                    )
                ),
            )
        )

        val scene1 = Scene(
            name = "JUnit Test",
            parameterSet = listOf(
                ParameterSet(
                    baseChannel = 29,
                    parameters = mutableListOf(
                        IntParameter("MasterDimmer", 255),
                        RGBWColor(0, 255, 0, 0)
                    )
                ),
                ParameterSet(
                    baseChannel = 15,
                    parameters = mutableListOf(
                        IntParameter("MasterDimmer", 255),
                        RGBWColor(255, 0, 0, 0)
                    )
                ),
            )
        )
        scene1.write(preferences,)

        val scene2 = Scene(
            name = "JUnit Test",
            parameterSet = listOf(
                ParameterSet(
                    baseChannel = 15,
                    parameters = mutableListOf(
                        IntParameter("MasterDimmer", 255),
                        RGBWColor(0, 255, 0, 0)
                    )
                ),
                ParameterSet(
                    baseChannel = 29,
                    parameters = mutableListOf(
                        IntParameter("MasterDimmer", 255),
                        RGBWColor(255, 0, 0, 0)
                    )
                ),
            )
        )

        scene0.fade(scene1, 1000, preferences)
        scene1.fade(scene2, 2000, preferences)
        Thread.sleep(2000)
        scene2.fade(scene1, 2000, preferences)
        scene1.fade(scene0, 1000, preferences)
    }
}
