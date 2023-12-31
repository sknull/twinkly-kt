package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "Menu")
class Menu : AbstractMenuProvider() {
    @JacksonXmlProperty(localName = "Func", isAttribute = true)
    val function: String? = null

    @JacksonXmlProperty(localName = "Func_Ex", isAttribute = true)
    val functionExtension: String? = null

    @JacksonXmlProperty(localName = "Title_1", isAttribute = true)
    override val key: String? = null

    @JacksonXmlProperty(localName = "YNC_Tag", isAttribute = true)
    val yncTag: String? = null

    @JacksonXmlProperty(localName = "List_Type", isAttribute = true)
    val listType: String? = null

    @JacksonXmlProperty(localName = "Icon_on", isAttribute = true)
    val iconOn: String? = null

    @JacksonXmlProperty(localName = "Disp", isAttribute = true)
    val display: String? = null

    @JacksonXmlProperty(localName = "Playable", isAttribute = true)
    val playable: String? = null

    @JacksonXmlProperty(localName = "Put_1")
    @JacksonXmlElementWrapper(localName = "Put_1", useWrapping = false)
    val put1: List<Put1> = listOf()

    @JacksonXmlProperty(localName = "Put_2")
    @JacksonXmlElementWrapper(localName = "Put_2", useWrapping = false)
    val put2: List<Put2> = listOf()

    @JacksonXmlProperty(localName = "Get")
    val get: Get? = null

    @JacksonXmlProperty(localName = "Cmd_List")
    val commandList: CmdList? = null

    @JacksonXmlProperty(localName = "FKey")
    @JacksonXmlElementWrapper(localName = "FKey", useWrapping = false)
    val fkey: List<FKey> = listOf()

    @JacksonXmlProperty(localName = "SKey")
    val skey: SKey? = null
    val name: String
        get() {
            var name = key
            if (name.isNullOrEmpty()) {
                name = function!!.replace("_", "-") + " " + functionExtension!!.replace("_", "-")
            }
            return name
        }
    val subUnitName: String?
        get() {
            var subUnitName: String? = null
            var menu: AbstractMenuProvider? = this
            while (menu != null) {
                if (menu is Menu && "Subunit" == menu.function) {
                    subUnitName = menu.yncTag
                    break
                }
                menu = menu.parent
            }
            return subUnitName
        }

    fun createCommand(vararg params: String?): String {
        val sb = StringBuilder()
        val sections: MutableList<String> = get?.command?.value?.split(",")?.toMutableList()?: mutableListOf()
        val subUnitName = subUnitName
        if (subUnitName != null) {
            sections.add(0, subUnitName)
        }
        sb.append("<YAMAHA_AV cmd=\"PUT\">")
        var index = 0
        val tagsToClose: MutableList<String> = mutableListOf()
        for (section in sections) {
            if (!section.contains("=")) {
                if (!tagsToClose.contains(section)) {
                    tagsToClose.add(0, section)
                    sb.append("<").append(section).append(">")
                }
            }
            else {
                val elems = section.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val tagName = elems[0]
                sb.append("<").append(tagName).append(">")
                val paramIndex = elems[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                val value = determineParameterValue(paramIndex)
                if (value != null) {
                    sb.append(value)
                }
                else {
                    sb.append(params[index++])
                }
                sb.append("</").append(tagName).append(">")
            }
        }

        // close remaining tags
        for (section in tagsToClose) {
            sb.append("</").append(section).append(">")
        }
        sb.append("</YAMAHA_AV>")
        return sb.toString()
    }

    private fun determineParameterValue(index: String): String? {
        var value: String? = null
        var param: Param? = null
        if ("Param_1" == index) {
            param = get?.param1
        }
        else if ("Param_2" == index) {
            param = get?.param2
        }
        else if ("Param_3" == index) {
            param = get?.param3
        }
        if (param != null) {
            val direct = param.direct
            if (direct != null) {
                if (direct.size == 1) {
                    value = direct[0].value
                }
            }
        }
        return value
    }
}
