package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText


class Text : XmlEntity() {
    @JacksonXmlText
    val value: String? = null
}
