package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import java.util.Objects
import java.util.function.Consumer

class LMZone(
    val id: Int? = null,
    val name: String? = null,
    val logo: String? = null,
    val tempChannel: Int? = null,
    val arrow: String? = null,
    val actors: MutableList<LMActor> = ArrayList()
) : Comparable<LMZone>, HtmlRenderable {

    fun addActor(actor: LMActor) {
        actors.add(actor)
    }

    override fun toHtml(configHolder: ConfigHolder): String {
        val sb = StringBuilder()
        sb.append("  <div class=\"group\">\n")
        renderLabel(sb, name)
        sb.append("    <div class=\"sub-group\">\n")
        actors.forEach(Consumer { actor: LMActor -> sb.append(actor.toHtml(configHolder)) })
        sb.append("    </div><!-- sub-group -->\n")
            .append("  </div><!-- group -->\n")
        return sb.toString()
    }

    override fun compareTo(o: LMZone): Int {
        return name!!.lowercase().compareTo(o.name?.lowercase()?:"")
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val lmZone = o as LMZone
        return id == lmZone.id && name == lmZone.name
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name)
    }
}
