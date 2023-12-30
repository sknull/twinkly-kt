package de.visualdigits.kotlin.klanglicht.rest.shelly.model.html

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import de.visualdigits.kotlin.klanglicht.model.shelly.ShellyDevice
import de.visualdigits.kotlin.klanglicht.model.shelly.status.Status
import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html.HtmlRenderable
import de.visualdigits.kotlin.klanglicht.rest.shelly.handler.ShellyHandler

class ShellyStatus : HtmlRenderable {

    fun toHtml(shellyHandler: ShellyHandler): String {
        val sb = StringBuilder()
        sb.append("<div class=\"title\" onclick=\"toggleFullScreen();\" alt=\"Toggle Fullscreen\" title=\"Toggle Fullscreen\">")
            .append("Current Power Values")
            .append("</div>\n")
        sb.append("<div class=\"category\">\n")
        renderLabel(sb, "Shelly Dashboard")
        shellyHandler.status().forEach { device: ShellyDevice, status: Status ->
            sb.append("  <div class=\"floatgroup\">\n")
            renderLabel(sb, device.name)
            sb.append("    <div class=\"sub-group\">\n")

            // power
            var power = listOf(0.0)
            var bgColor = "#aaaaaa"
            val isOnline = "offline" != status.mode
            if (isOnline) {
                power = status.meters?.map { it.power?:0.0 }?:listOf(0.0)
                val totalPower = power.reduce { a, b -> a + b }
                bgColor = if (totalPower > 0.0) "red" else "green"
            }
            renderPanel(sb, "textpanel", bgColor, "Power&nbsp;$power")

            // on/off status
            var isOn = false
            val lightColors: MutableList<String> = ArrayList()
            if (isOnline) {
                val relays = status.relays
                if (relays != null) {
                    for (relay in relays) {
                        if (relay.isOn == true) {
                            isOn = true
                            break
                        }
                    }
                    bgColor = if (isOn) "red" else "green"
                } else {
                    val lights = status.lights
                    if (lights != null) {
                        for (light in lights) {
                            lightColors.add(RGBColor(light.red!!, light.green!!, light.blue!!).web())
                            if (light.isOn == true) {
                                isOn = true
                            }
                        }
                        bgColor = if (isOn) "red" else "green"
                    } else {
                        bgColor = "#ff00ff"
                    }
                }
            }
            renderPanel(sb, "circle", bgColor, if (isOn) "on" else "off")
            for (lightColor in lightColors) {
                renderPanel(sb, "circle", lightColor, "")
            }
            sb.append("    </div> <!-- sub-group -->\n")
            sb.append("  </div> <!-- group -->\n") // group
        }
        sb.append("</div > <!-- category -->\n") // category
        return sb.toString()
    }

    private fun renderPanel(sb: StringBuilder, clazz: String, bgColor: String, value: String?) {
        sb.append("      <div class=\"").append(clazz).append("\" style=\"background-color:").append(bgColor)
            .append("\" >\n")
        if (value != null && !value.isEmpty()) {
            renderLabel(sb, value)
        }
        sb.append("      </div> <!-- ").append(clazz).append(" -->\n")
    }

    override fun toHtml(configHolder: ConfigHolder): String? {
        return null
    }
}
