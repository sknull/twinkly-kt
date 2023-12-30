package de.visualdigits.kotlin.klanglicht.model.lightmanager.xml

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


@JacksonXmlRootElement
class Actuator(
    val name: String? = null,
    val type: String? = null,
    val steps: Long? = null,
    @JacksonXmlElementWrapper(localName = "commandlist") val commandList: List<Command> = listOf()
)

