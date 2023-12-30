package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


@JacksonXmlRootElement(localName = "SKey")
class SKey : XmlEntity() {
    @JacksonXmlProperty(localName = "Title", isAttribute = true)
    val title: String? = null

    @JacksonXmlProperty(localName = "Path")
    val path: Path? = null

    @JacksonXmlProperty(localName = "Play")
    val play: Locator? = null

    @JacksonXmlProperty(localName = "Pause")
    val pause: Locator? = null

    @JacksonXmlProperty(localName = "Stop")
    val stop: Locator? = null

    @JacksonXmlProperty(localName = "Fwd")
    val fwd: Locator? = null

    @JacksonXmlProperty(localName = "Rev")
    val rev: Locator? = null
}
