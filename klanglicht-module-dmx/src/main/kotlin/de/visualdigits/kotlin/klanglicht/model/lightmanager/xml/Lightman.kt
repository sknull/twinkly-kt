package de.visualdigits.kotlin.klanglicht.model.lightmanager.xml

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.io.IOException
import java.io.InputStream

@JacksonXmlRootElement
class Lightman(
    @JacksonXmlElementWrapper(localName = "lightscenes") val lightScenes: List<Scene> = listOf(),
    @JacksonXmlProperty(localName = "zone") @JacksonXmlElementWrapper(useWrapping = false) val zones: List<Zone> = listOf()
) {

    companion object {
        val MAPPER: XmlMapper = XmlMapper()
        fun load(ins: InputStream?): Lightman {
            val lightman: Lightman
            lightman = try {
                MAPPER.readValue<Lightman>(ins, Lightman::class.java)
            } catch (e: IOException) {
                throw IllegalStateException("Could not load xml file", e)
            }
            return lightman
        }
    }
}
