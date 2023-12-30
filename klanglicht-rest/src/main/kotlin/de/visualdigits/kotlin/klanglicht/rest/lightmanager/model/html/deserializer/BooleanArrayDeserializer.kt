package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class BooleanArrayDeserializer : JsonDeserializer<BooleanArray>() {

    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): BooleanArray {
        val sStates = jsonParser.text
        val stateChars = sStates.toCharArray()
        val n = sStates.length
        val states = BooleanArray(n)
        for (i in 0 until n) {
            states[i] = stateChars[i] == '1'
        }
        return states
    }
}
