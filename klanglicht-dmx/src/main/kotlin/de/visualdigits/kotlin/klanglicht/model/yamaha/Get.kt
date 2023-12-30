package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


@JacksonXmlRootElement(localName = "Get")
class Get : de.visualdigits.kotlin.klanglicht.model.yamaha.XmlEntity() {
    @JacksonXmlProperty(localName = "Cmd")
    val command: de.visualdigits.kotlin.klanglicht.model.yamaha.Cmd? = null

    @JacksonXmlProperty(localName = "Param_1")
    val param1: de.visualdigits.kotlin.klanglicht.model.yamaha.Param? = null

    @JacksonXmlProperty(localName = "Param_2")
    val param2: de.visualdigits.kotlin.klanglicht.model.yamaha.Param? = null

    @JacksonXmlProperty(localName = "Param_3")
    val param3: de.visualdigits.kotlin.klanglicht.model.yamaha.Param? = null
}
