package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.util.function.Consumer

abstract class AbstractMenuProvider : de.visualdigits.kotlin.klanglicht.model.yamaha.XmlEntity() {

    @JacksonXmlProperty(localName = "Menu")
    @JacksonXmlElementWrapper(localName = "Menu", useWrapping = false)
    var menus: List<de.visualdigits.kotlin.klanglicht.model.yamaha.Menu> = listOf()

    @JsonIgnore
    var parent: de.visualdigits.kotlin.klanglicht.model.yamaha.AbstractMenuProvider? = null

    @JsonIgnore
    val tree: MutableMap<String?, de.visualdigits.kotlin.klanglicht.model.yamaha.Menu> = mutableMapOf()

    abstract val key: String?

    fun <T : de.visualdigits.kotlin.klanglicht.model.yamaha.AbstractMenuProvider> getMenu(path: String): T {
        var provider: de.visualdigits.kotlin.klanglicht.model.yamaha.AbstractMenuProvider? = this
        for (key in path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            provider = provider!!.tree[key]
        }
        return provider as T
    }

    fun initializeTree() {
        if (menus != null) {
            menus.forEach(Consumer { menu: de.visualdigits.kotlin.klanglicht.model.yamaha.Menu ->
                menu.parent = this
                tree[menu.name] = menu
                menu.initializeTree()
            })
        }
    }

    protected fun renderTree(indent: String, sb: StringBuilder) {
        tree.forEach { (key: String?, subMenu: de.visualdigits.kotlin.klanglicht.model.yamaha.Menu) ->
            sb.append(indent).append(key).append("\n")
            subMenu.renderTree("$indent  ", sb)
        }
    }
}
