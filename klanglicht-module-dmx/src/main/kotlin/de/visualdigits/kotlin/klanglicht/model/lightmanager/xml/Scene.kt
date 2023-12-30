package de.visualdigits.kotlin.klanglicht.model.lightmanager.xml

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


@JacksonXmlRootElement
class Scene(
    val name: String? = null,
    val param: String? = null
)
