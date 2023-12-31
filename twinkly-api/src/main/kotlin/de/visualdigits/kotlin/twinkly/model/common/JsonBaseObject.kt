package de.visualdigits.kotlin.twinkly.model.common

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import java.io.File

abstract class JsonBaseObject {

    companion object {
        val mapper: ObjectMapper = jacksonMapperBuilder()
//            .disable(SerializationFeature.INDENT_OUTPUT)
            .build()
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)

        inline fun <reified T : JsonBaseObject> unmarshall(file: File) : T {
            return mapper.readValue(file, T::class.java)
        }

        inline fun <reified T : JsonBaseObject> unmarshall(json: String) : T {
            return mapper.readValue(json, T::class.java)
        }
    }

    override fun toString(): String = this::class.simpleName + ": " + marshall()

    fun marshall(): String {
        return mapper.writeValueAsString(this)
    }

    fun marshallToBytes(): ByteArray {
        return mapper.writeValueAsBytes(this)
    }
}
