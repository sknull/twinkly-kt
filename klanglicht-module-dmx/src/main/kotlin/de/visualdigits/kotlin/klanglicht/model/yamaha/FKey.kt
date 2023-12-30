package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


@JacksonXmlRootElement(localName = "FKey")
class FKey : XmlEntity() {
    @JacksonXmlProperty(localName = "Title", isAttribute = true)
    val title: String? = null

    @JacksonXmlProperty(localName = "Path")
    val path: Path? = null

    @JacksonXmlProperty(localName = "F1")
    val f1: F? = null

    @JacksonXmlProperty(localName = "F2")
    val f2: F? = null

    @JacksonXmlProperty(localName = "F3")
    val f3: F? = null
}
