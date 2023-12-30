package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class NumericBooleanDeserializer : JsonDeserializer<Boolean>() {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Boolean {
        return "0" != parser.text
    }
}
