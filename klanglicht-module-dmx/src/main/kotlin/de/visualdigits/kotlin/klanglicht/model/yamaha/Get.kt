package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


@JacksonXmlRootElement(localName = "Get")
class Get : XmlEntity() {
    @JacksonXmlProperty(localName = "Cmd")
    val command: Cmd? = null

    @JacksonXmlProperty(localName = "Param_1")
    val param1: Param? = null

    @JacksonXmlProperty(localName = "Param_2")
    val param2: Param? = null

    @JacksonXmlProperty(localName = "Param_3")
    val param3: Param? = null
}
