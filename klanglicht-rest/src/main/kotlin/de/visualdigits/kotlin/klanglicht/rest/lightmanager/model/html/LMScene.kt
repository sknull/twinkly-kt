package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import org.apache.commons.lang3.StringUtils


class LMScene(
    var id: Int? = null,
    var name: String? = null,
    var color: String? = null
) : HtmlRenderable {
    override fun toHtml(configHolder: ConfigHolder): String {
        return toHtml(configHolder, "")
    }

    fun toHtml(configHolder: ConfigHolder, group: String): String {
        val lightmanagerUrl = configHolder.preferences?.serviceMap?.get("lmair")?.url
        val sb = StringBuilder()
        sb.append("      <div class=\"button\"")
        if (StringUtils.isNotEmpty(color)) {
            if (color?.contains(",") == true) {
                sb.append(" style=\"background: -moz-linear-gradient(left, ")
                    .append(color)
                    .append("); background: -webkit-linear-gradient(left, ")
                    .append(color)
                    .append("); background: linear-gradient(to right, ")
                    .append(color)
                    .append(");\"")
            }
            else {
                sb.append(" style=\"background-color: ")
                    .append(color)
                    .append(";\"")
            }
        }
        val label = normalizeLabel(group)
        sb.append("><input type=\"submit\" value=\"")
            .append(label)
            .append("\" onclick=\"request('")
            .append(lightmanagerUrl)
            .append("/control?scene=")
            .append(id)
            .append("');\"/></div>\n")
        return sb.toString()
    }

    /**
     * Removes the group name from the label (if matches).
     *
     * @param group The current group this scene belongs to.
     *
     * @return String
     */
    private fun normalizeLabel(group: String): String {
        var label = name
        if (label?.lowercase()?.startsWith(group.lowercase()) == true) {
            label = label.substring(group.length).trim { it <= ' ' }
        }
        return label?:""
    }
}
