package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "Cmd_List")
class CmdList : de.visualdigits.kotlin.klanglicht.model.yamaha.XmlEntity() {
    @JacksonXmlProperty(localName = "Define")
    @JacksonXmlElementWrapper(localName = "Define", useWrapping = false)
    val define: List<de.visualdigits.kotlin.klanglicht.model.yamaha.Define> = listOf()
}
