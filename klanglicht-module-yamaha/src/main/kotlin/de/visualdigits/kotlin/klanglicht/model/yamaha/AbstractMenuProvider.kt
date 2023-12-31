package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.util.function.Consumer

abstract class AbstractMenuProvider : XmlEntity() {

    @JacksonXmlProperty(localName = "Menu")
    @JacksonXmlElementWrapper(localName = "Menu", useWrapping = false)
    var menus: List<Menu> = listOf()

    @JsonIgnore
    var parent: AbstractMenuProvider? = null

    @JsonIgnore
    val tree: MutableMap<String?, Menu> = mutableMapOf()

    abstract val key: String?

    fun <T : AbstractMenuProvider> getMenu(path: String): T {
        var provider: AbstractMenuProvider? = this
        for (key in path.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            provider = provider!!.tree[key]
        }
        return provider as T
    }

    fun initializeTree() {
        if (menus != null) {
            menus.forEach(Consumer { menu: Menu ->
                menu.parent = this
                tree[menu.name] = menu
                menu.initializeTree()
            })
        }
    }

    protected fun renderTree(indent: String, sb: StringBuilder) {
        tree.forEach { (key: String?, subMenu: Menu) ->
            sb.append(indent).append(key).append("\n")
            subMenu.renderTree("$indent  ", sb)
        }
    }
}
