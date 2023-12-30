package de.visualdigits.kotlin.klanglicht.model.yamaha

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.io.IOException
import java.io.InputStream

@JacksonXmlRootElement(localName = "Unit_Description")
class UnitDescription : de.visualdigits.kotlin.klanglicht.model.yamaha.AbstractMenuProvider() {

    @JacksonXmlProperty(localName = "Version", isAttribute = true)
    val version: String? = null

    @JacksonXmlProperty(localName = "Unit_Name", isAttribute = true)
    override val key: String? = null

    @JacksonXmlProperty(localName = "Language")
    val language: de.visualdigits.kotlin.klanglicht.model.yamaha.Language? = null

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(key).append(" [").append(version).append("]\n")
        renderTree("", sb)
        return sb.toString()
    }

    val dspPrograms: Map<String, String>
        get() {
            val menu = getMenu<de.visualdigits.kotlin.klanglicht.model.yamaha.Menu>("Main Zone/Setup/Surround/Program")
            return menu?.put2?.get(0)?.param1?.direct?.map { direct ->
                Pair(direct.value!!, direct.iconOn!!)
            }?.toMap()?:mapOf()
        }

    companion object {
        val mapper: XmlMapper = XmlMapper()

        init {
            de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription.Companion.mapper.enable(SerializationFeature.INDENT_OUTPUT)
        }

        fun load(ins: InputStream): de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription {
            val unitDescription: de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription
            unitDescription = try {
                de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription.Companion.mapper.readValue<de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription>(ins, de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription::class.java)
            } catch (e: IOException) {
                throw IllegalStateException("Could not unmarshall file: $ins", e)
            }
            unitDescription.initializeTree()
            return unitDescription
        }

        fun load(xml: String?): de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription {
            val unitDescription: de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription
            unitDescription = try {
                de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription.Companion.mapper.readValue<de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription>(xml, de.visualdigits.kotlin.klanglicht.model.yamaha.UnitDescription::class.java)
            } catch (e: IOException) {
                throw IllegalStateException("Could not unmarshall xml", e)
            }
            unitDescription.initializeTree()
            return unitDescription
        }
    }
}
