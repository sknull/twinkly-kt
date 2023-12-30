package de.visualdigits.kotlin.klanglicht.rest.lightmanager.widgets

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html.HtmlRenderable


class ColorWheelOddEven(
    val id: String? = null
) : HtmlRenderable {

    override fun toHtml(configHolder: ConfigHolder): String {
        return "    <div class=\"colorwheel-wrapper\">\n" +
                "\t\t<div class=\"colorwheel-title\"><span class=\"label\">COLORPICKER</span></div>\n" +
                "\t\t<div class=\"colorwheel-panel\">\n" +
                "\t\t\t<div class=\"color-wheel\" id=\"" + id + "_odd\"></div>\n" +
                "\t\t\t<div class=\"color-wheel\" id=\"" + id + "_even\"></div>\n" +
                "\t\t</div>\n" +
                "        <script type=\"application/javascript\">\n" +
                "            var colorWheelOdd = new iro.ColorPicker(\"#" + id + "_odd\", {\n" +
                "                wheelLightness: false\n" +
                "            });\n" +
                "            var colorWheelEven = new iro.ColorPicker(\"#" + id + "_even\", {\n" +
                "                wheelLightness: false\n" +
                "            });\n" +
                "\n" +
                "            colorWheel.on('color:change', function(color, changes){\n" +
                "                var colorOdd = colorWheelOdd.color.hexString.substring(1);\n" +
                "                var colorEven = colorWheelEven.color.hexString.substring(1);\n" +
                "                var colors = colorOdd + \",\" + colorEven + \",\" + colorOdd + \",\" + colorEven + \",\" + colorOdd + \",\" + colorEven + \",\" + colorOdd\n" +
                "                Colors = colors;\n" +
                "            });\n" +
                "        </script>\n" +
                "    </div>"
    }
}
