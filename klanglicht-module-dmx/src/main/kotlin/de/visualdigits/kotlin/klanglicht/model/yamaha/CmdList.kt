package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "Cmd_List")
class CmdList : XmlEntity() {
    @JacksonXmlProperty(localName = "Define")
    @JacksonXmlElementWrapper(localName = "Define", useWrapping = false)
    val define: List<Define> = listOf()
}
