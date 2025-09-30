package de.visualdigits.kotlin.util

/**
 * Ensures that the returned hex string is appropriate for the given
 * number of color channels.
 */
fun String.ensureHexLength(numberOfChannels: Int): String {
    val numberOfDigits = 2 * numberOfChannels
    var s = replace("0x", "#")
    if (!s.startsWith("#")) {
        s = "#$s"
    }
    val l = s.length - 1
    if (l < numberOfDigits) {
        s += "0".repeat(numberOfDigits - l)
    }
    return s
}
