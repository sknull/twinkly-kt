package de.visualdigits.kotlin.klanglicht.rest.lightmanager.widgets

import de.visualdigits.kotlin.klanglicht.model.color.RGBColor
import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html.HtmlRenderable
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ColorWheel(
    val id: String? = null
) : HtmlRenderable {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun toHtml(configHolder: ConfigHolder): String {
        val wheelId = id!!.replace(" ", "")
        val lastColorState = configHolder.getFadeable(id)
        val hexColor = lastColorState?.getRgbColor()?.web()?:"#000000"
        log.debug("Got color '${RGBColor(hexColor).ansiColor()}' for id '$id'")
        return "\t<div class=\"colorwheel-wrapper\">\n" +
                "\t\t<div class=\"colorwheel-title\"><span class=\"label\">COLORPICKER - " + id + "</span></div>\n" +
                "\t\t<div class=\"colorwheel-panel\">\n" +
                "\t\t\t<div class=\"color-wheel\" id=\"colorwheel-" + wheelId + "\"></div>\n" +
                "\t\t</div>\n" +
                "\t\t<script type=\"application/javascript\">\n" +
                "\t\t\tvar colorWheel" + wheelId + " = new iro.ColorPicker(\"#colorwheel-" + wheelId + "\", {\n" +
                "\t\t\t\twheelLightness: false,\n" +
                "\t\t\t\tcolor: \"" + hexColor + "\"\n" +
                "\t\t\t});\n" +
                "\n" +
                "\t\t\tcolorWheel" + wheelId + ".on('color:change', function(color, changes){\n" +
                "\t\t\t\tvar colorOdd = colorWheel" + wheelId + ".color.hexString.substring(1);\n" +
                "\t\t\t\tvar colorEven = \"000000\";\n" +
                "\t\t\t\tColors" + wheelId + " = colorOdd, colorEven;\n" +
                "\t\t\t});\n" +
                "\t\t</script>\n" +
                "\t\t</div>"
    }
}
