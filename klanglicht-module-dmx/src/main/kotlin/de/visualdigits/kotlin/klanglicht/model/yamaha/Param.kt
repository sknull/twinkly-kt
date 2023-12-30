package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty


class Param : XmlEntity() {
    @JacksonXmlProperty(localName = "Func")
    var function: String? = null

    @JacksonXmlProperty(localName = "Direct")
    @JacksonXmlElementWrapper(localName = "Direct", useWrapping = false)
    var direct: List<Direct> = listOf()

    @JacksonXmlProperty(localName = "Indirect")
    var indirect: Indirect? = null

    @JacksonXmlProperty(localName = "Range")
    var range: Range? = null

    @JacksonXmlProperty(localName = "Text")
    var text: Text? = null
}
