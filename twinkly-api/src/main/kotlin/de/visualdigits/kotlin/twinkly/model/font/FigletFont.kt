package de.visualdigits.kotlin.twinkly.model.font

import java.nio.charset.StandardCharsets

class FigletFont(
    resourcePath: String
) {
    val classLoader = Thread.currentThread().contextClassLoader

    private val MAGIC_NUMBER = "^[tf]lf2.".toRegex()
    private val END_MARKER = "(.)\\s*$".toRegex()
    private val HEX_MATCH = "(?i)^0x".toRegex()

    var hardBlank: String = ""
    var height: Int = 0
    var baseLine: Int = 0
    private var maxLength: Int = 0
    private var oldLayout: Int = 0
    private var commentLines: Int = 0
    var printDirection: Int = 0
    var smushMode: Int = 0

    val chars: MutableMap<Int, List<String>> = mutableMapOf()
    val width: MutableMap<Int, Int> = mutableMapOf()

    init {
        val ins = classLoader.getResourceAsStream(resourcePath)
        var lines = ins?.bufferedReader(StandardCharsets.UTF_8).use { reader ->
            reader?.readLines()
        }?: listOf()

        val header: String = lines.first()
        require (MAGIC_NUMBER.findAll(header).toList().isNotEmpty()) { "Invalid font: $resourcePath" }
        val headerParts = MAGIC_NUMBER.replace(header, "").split(" ")
        val n = headerParts.size
        require (n >= 6) { "Invalid header for font: $resourcePath" }
        this.hardBlank = headerParts[0][0].toString()
        this.height = headerParts[1].toInt()
        this.baseLine = headerParts[2].toInt()
        this.maxLength = headerParts[3].toInt()
        this.oldLayout = headerParts[4].toInt()
        this.commentLines = headerParts[5].toInt()
        this.printDirection = if (n > 6) {
            headerParts[6].toInt()
        } else {
            0
        }
        var fullLayout = if (n > 7) {
            headerParts[7].toInt()
        } else {
            0
        }
        if (fullLayout == 0) {
            fullLayout = if (this.oldLayout == 0) {
                64
            } else if (this.oldLayout < 0) {
                0
            } else {
                (this.oldLayout and 31) or 128
            }
        }
        this.smushMode = fullLayout

        lines = lines.drop(1).drop(commentLines)

        // Read ASCII standard character set 32 - 127
        for (i in 32 .. 127) {
            lines = readChar(i, lines)
        }

        // Read ASCII extended character set
        var i = 127
        while (lines.isNotEmpty()) {
            val line = lines.first().trim()
            val ii = line.split(" ")[0]
            val idx: Int? = if (isNumeric(ii)) {
                ii.toInt()
            } else if (HEX_MATCH.find(ii) != null) {
                Integer.valueOf(ii.substring(2), 16).toInt()
            } else {
                null
            }
            if (idx != null) {
                lines = lines.drop(1)
                i = idx
            }
            lines = readChar(i, lines)
            i += 1
        }
    }

    fun readChar(i: Int, l: List<String>): List<String> {
        var lines = l
        var end: Regex? = null
        var width = 0
        val chrs = mutableListOf<String>()
        for (dummy in 0 until this.height) {
            var line = lines.first()
            lines = lines.drop(1)
            if (end == null) {
                end = END_MARKER.find(line)?.let { x -> Regex(Regex.escape(x.value) + "{1,2}$") }
            }
            end?.let { x -> line = x.replace(line, "") }
            if (line.length > width) {
                width = line.length
            }
            chrs.add(line)
        }
        if (chrs.joinToString("").isNotEmpty()) {
            this.width.put(i, width)
            this.chars.put(i, chrs.toList())
        }
        return lines
    }

    fun isNumeric(str: String): Boolean {
        return try {
            str.toLong()
            str.toDouble()
            true
        } catch (nfe: NumberFormatException) {
             false
        }
    }
}
