package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


@JacksonXmlRootElement(localName = "SKey")
class SKey : de.visualdigits.kotlin.klanglicht.model.yamaha.XmlEntity() {
    @JacksonXmlProperty(localName = "Title", isAttribute = true)
    val title: String? = null

    @JacksonXmlProperty(localName = "Path")
    val path: de.visualdigits.kotlin.klanglicht.model.yamaha.Path? = null

    @JacksonXmlProperty(localName = "Play")
    val play: de.visualdigits.kotlin.klanglicht.model.yamaha.Locator? = null

    @JacksonXmlProperty(localName = "Pause")
    val pause: de.visualdigits.kotlin.klanglicht.model.yamaha.Locator? = null

    @JacksonXmlProperty(localName = "Stop")
    val stop: de.visualdigits.kotlin.klanglicht.model.yamaha.Locator? = null

    @JacksonXmlProperty(localName = "Fwd")
    val fwd: de.visualdigits.kotlin.klanglicht.model.yamaha.Locator? = null

    @JacksonXmlProperty(localName = "Rev")
    val rev: de.visualdigits.kotlin.klanglicht.model.yamaha.Locator? = null
}
