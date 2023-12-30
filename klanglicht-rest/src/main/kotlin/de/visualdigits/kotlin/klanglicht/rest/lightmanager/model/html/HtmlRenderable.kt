package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder

interface HtmlRenderable {
    fun toHtml(configHolder: ConfigHolder): String?
    fun renderLabel(sb: StringBuilder, label: String?) {
        sb.append("<span class=\"label\">").append(label).append("</span>\n")
    }
}
