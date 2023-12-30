package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html

import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.common.collect.LinkedListMultimap
import com.google.common.collect.Multimap
import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.widgets.ColorWheel
import org.apache.commons.lang3.StringUtils
import java.util.TreeMap
import java.util.function.Consumer

class LMScenes(
    val name: String? = null
) : HtmlRenderable {
    val COLOR_WHEEL_GROUPS: List<String> = mutableListOf("Dmx", "Deko", "Rgbw", "Bar", "Starwars")

    @JsonIgnore
    val scenes: Multimap<String, LMScene> = LinkedListMultimap.create<String, LMScene>()
    fun add(scene: LMScene) {
        var group: String? = "common"
        val attributes = LMNamedAttributes(scene.name, "group", "color")
        if (attributes.matched()) {
            val name = attributes.name
            if (StringUtils.isNotEmpty(name)) {
                scene.name = name
            }
            val g = attributes["group"]
            if (StringUtils.isNotEmpty(g)) {
                group = g
            }
            scene.color = attributes["color"]
        }
        if ("hidden" != group) {
            scenes.put(StringUtils.capitalize(group), scene)
        }
    }

    override fun toHtml(configHolder: ConfigHolder): String {
        val sb = StringBuilder()
        sb.append("<div class=\"title\" onclick=\"toggleFullScreen();\" alt=\"Toggle Fullscreen\" title=\"Toggle Fullscreen\">")
            .append(name)
            .append("</div>\n")
        sb.append("<div class=\"category\">\n")
        renderLabel(sb, "S C E N E S")
        val scenesMap: Map<String, Collection<LMScene>> = TreeMap<String, Collection<LMScene>>(scenes.asMap())
        scenesMap.forEach { (group: String, groupScenes: Collection<LMScene>) ->
            renderScenesGroup(
                sb,
                configHolder,
                group,
                groupScenes
            )
        }
        sb.append("</div><!-- scenes -->\n\n")
        return sb.toString()
    }

    private fun renderScenesGroup(
        sb: StringBuilder,
        configHolder: ConfigHolder,
        group: String,
        groupScenes: Collection<LMScene>
    ) {
        val hasColorWheel = COLOR_WHEEL_GROUPS.contains(group)
        sb.append("  <div class=\"group")
        if (hasColorWheel) {
            sb.append(" has-colorwheel")
        }
        sb.append("\">\n")
        renderLabel(sb, group)
        sb.append("    <div class=\"sub-group")
        if (hasColorWheel) {
            sb.append(" has-colorwheel")
        }
        sb.append("\">\n")
        groupScenes.forEach(Consumer { scene: LMScene -> sb.append(scene.toHtml(configHolder, group)) })
        sb.append("    </div><!-- sub-group -->\n")
        sb.append("  </div><!-- group -->\n")
        if (hasColorWheel) {
            val html: String = ColorWheel(group).toHtml(configHolder)
            sb.append(html)
        }
    }
}
