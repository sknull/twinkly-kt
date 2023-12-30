package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html

import java.util.TreeMap
import java.util.regex.Pattern

class LMNamedAttributes(
    s: String?,
    vararg attributes: String
) {

    private var matched = false
    var name: String? = null
    val attributesMap: MutableMap<String, String> = TreeMap()

    init {
        val matcherParams = P_PARAMS.matcher(s)
        matched = matcherParams.find()
        if (matched) {
            name = matcherParams.group(1).trim { it <= ' ' }
            val params = matcherParams.group(2).trim { it <= ' ' }
            for (attribute in attributes) {
                val pattern = Pattern.compile(attribute + PATTERN_TEMPLATE)
                val matcherSeparate = pattern.matcher(params)
                if (matcherSeparate.find()) {
                    attributesMap[attribute] = matcherSeparate.group(1).trim { it <= ' ' }
                }
            }
        }
    }

    fun matched(): Boolean {
        return matched
    }

    operator fun get(attribute: String): String {
        return getOrDefault(attribute, "")
    }

    fun getOrDefault(attribute: String, defaultValue: String): String {
        return attributesMap.getOrDefault(attribute, defaultValue)
    }

    companion object {
        val P_PARAMS = Pattern.compile("^([^{]*)\\{?([^}]*)\\}?.*$")
        private const val PATTERN_TEMPLATE = ":([^;}]*)"
    }
}
