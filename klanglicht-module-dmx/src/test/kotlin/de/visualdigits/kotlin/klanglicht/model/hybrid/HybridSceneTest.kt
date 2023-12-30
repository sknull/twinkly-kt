package de.visualdigits.kotlin.klanglicht.model.hybrid

import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences
import org.apache.commons.lang3.SystemUtils
import org.junit.jupiter.api.Test
import java.io.File

class HybridSceneTest {

    val preferences = Preferences.load(File(SystemUtils.getUserHome(), ".klanglicht"))

    @Test
    fun testFade() {
        val scene1 = HybridScene(
            ids = "Starwars,Rgbw,15,29,Bar",
            hexColors = "#ff0000,#00ff00,#0000ff,#ffff00,#00ffff",
            gains = "",
            preferences = preferences
        )
//        println(scene1)
        val scene2 = HybridScene(
            ids = "Starwars,Rgbw,15,29,Bar",
            hexColors = "#00ffff,#ff00ff,#ffff00,#0000ff,#ff0000",
            gains = "",
            preferences = preferences
        )
        Thread.sleep(5000)
//        println(scene2)
//        println()
//        val n = 10
//        for (f in 0 until n) {
//            val faded = scene1.fade(scene2, f.toDouble() / n)
//            println(faded)
//        }
    }
}
