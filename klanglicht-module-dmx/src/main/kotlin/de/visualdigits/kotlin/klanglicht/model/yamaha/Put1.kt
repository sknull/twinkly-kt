package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText


@JacksonXmlRootElement(localName = "Put_1")
class Put1 : XmlEntity() {
    @JacksonXmlProperty(localName = "Title_1", isAttribute = true)
    val title1: String? = null

    @JacksonXmlProperty(localName = "Func", isAttribute = true)
    val function: String? = null

    @JacksonXmlProperty(localName = "Func_Ex", isAttribute = true)
    val functionExtension: String? = null

    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    val id: String? = null

    @JacksonXmlProperty(localName = "Playable", isAttribute = true)
    val playable: String? = null

    @JacksonXmlProperty(localName = "Visible", isAttribute = true)
    val visible: String? = null

    @JacksonXmlProperty(localName = "Layout", isAttribute = true)
    val layout: String? = null

    @JacksonXmlText
    val value: String? = null
}
