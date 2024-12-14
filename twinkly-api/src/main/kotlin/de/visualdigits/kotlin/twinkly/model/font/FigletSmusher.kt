package de.visualdigits.kotlin.twinkly.model.font

class FigletSmusher(
    val direction: Direction = Direction.auto,
    val font: FigletFont
) {

    private val SM_EQUAL = 1 // smush equal chars (not hardblanks)
    private val SM_LOWLINE = 2 // smush _ with any char in hierarchy
    private val SM_HIERARCHY = 4 // hierarchy: |, /\, [], {}, (), <>
    private val SM_PAIR = 8 // hierarchy: [ + ] -> |, { + } -> |, ( + ) -> |
    private val SM_BIGX = 16 // / + \ -> X, > + < -> X
    private val SM_HARDBLANK = 32 // hardblank + hardblank -> hardblank
    private val SM_KERN = 64
    private val SM_SMUSH = 128
    private val PAIRS = listOf("[]", "{}", "()")

    fun smushRow(al: String, curChar: List<String>, row: Int, maxSmush: Int, curCharWidth: Int, prevCharWidth: Int): Pair<String, String> {
        var addLeft = al
        var addRight: String = curChar[row]

        when (direction) {
            Direction.rightToLeft -> {
                val temp = addRight
                addRight = addLeft
                addLeft = temp
            }
            else -> {}
        }

        for (i in 0 until maxSmush) {
            val (left, idx) = getLeftSmushedChar(i, addLeft, maxSmush)
            val right = addRight[i].toString()
            val smushed = smushChars(left, right, curCharWidth, prevCharWidth)
            addLeft = updateSmushedCharInLeftBuffer(addLeft, idx, smushed)
        }

        return Pair(addLeft, addRight)
    }

    private fun getLeftSmushedChar(i: Int, addLeft: String, maxSmush: Int): Pair<String, Int> {
        val idx = addLeft.length - maxSmush + i
        return Pair(if (idx >= 0 && idx < addLeft.length) {
            addLeft[idx].toString()
        } else {
            ""
        }, idx)
    }

    private fun updateSmushedCharInLeftBuffer(addLeft: String, idx: Int, smushed: String): String {
        return if (idx < 0 || idx > addLeft.length) {
            addLeft
        } else {
            val l: Array<String> = addLeft.toCharArray().map { it.toString() }.toTypedArray()
            l[idx] = smushed
            l.joinToString("")
        }
    }

    fun currentSmushAmount(buffer: Array<String>, curChar: List<String>, curCharWidth: Int, prevCharWidth: Int): Int {
        if ((font.smushMode and (SM_SMUSH or SM_KERN)) == 0) {
            return 0
        }
        var maxSmush = curCharWidth
        for (row in 0 until font.height) {
            var amt = currentRowSmushAmount(buffer, row, curChar, curCharWidth, prevCharWidth)
            if (amt < maxSmush) {
                maxSmush = amt
            }
        }
        return maxSmush
    }

    private fun currentRowSmushAmount(
        buffer: Array<String>,
        row: Int,
        curChar: List<String>,
        curCharWidth: Int,
        prevCharWidth: Int
    ): Int {
        var lineLeft = buffer[row]
        var lineRight = curChar[row]
        if (Direction.rightToLeft == direction) {
            val temp = lineLeft
            lineLeft = lineRight
            lineRight = temp
        }
        var linebd = rtrim(lineLeft).length - 1
        if (linebd < 0) {
            linebd = 0
        }
        var ch1: String = ""
        if (linebd < lineLeft.length) {
            ch1 = lineLeft[linebd].toString()
        }
        val charbd = lineRight.length - ltrim(lineRight).length
        var ch2: String = ""
        if (charbd < lineRight.length) {
            ch2 = lineRight[charbd].toString()
        }
        var amt = charbd + lineLeft.length - 1 - linebd

        if (ch1.isEmpty() || ch1 == " ") {
            amt += 1
        } else if (ch2.isNotEmpty() && smushChars(ch1, ch2, curCharWidth, prevCharWidth).isNotEmpty()) {
            amt += 1
        }
        return amt
    }

    private fun smushChars(left: String = "", right: String = "", curCharWidth: Int, prevCharWidth: Int): String {
        if (left.isNotEmpty() && left.trim().isEmpty()) {
            return right
        } else if (right.isNotEmpty() && right.trim().isEmpty()) {
            return left
        }

        if (prevCharWidth < 2 || curCharWidth < 2) {
            return ""
        }

        if ((font.smushMode and SM_SMUSH) == 0) {
            return ""
        }

        if ((font.smushMode and 63) == 0) {
            if (left == font.hardBlank) {
                return right
            }
            if (right == font.hardBlank) {
                return left
            }
            if (Direction.rightToLeft == direction) {
                return left
            } else {
                return right
            }
        }

        if ((font.smushMode and SM_HARDBLANK) != 0 && left == font.hardBlank && right == font.hardBlank) {
            return left
        }

        if (left == font.hardBlank || right == font.hardBlank) {
            return ""
        }

        if ((font.smushMode and SM_EQUAL) != 0 && left == right) {
            return left
        }

        determineSmushes().forEach { entry ->
            if (entry.key.contains(left) && entry.value.contains(right)) {
                return right
            }
            if (entry.value.contains(left) && entry.key.contains(right)) {
                return left
            }
        }

        if ((font.smushMode and SM_PAIR) != 0) {
            listOf(left + right, right + left)
                .forEach { pair ->
                    if (PAIRS.contains(pair)) {
                        return "|"
                    }
                }
        }

        if ((font.smushMode and SM_BIGX) != 0) {
            if ("/" == left && "\\" == right) {
                return "|"
            }
            if ("/" == right && "\\" == left) {
                return "Y"
            }
            if (">" == left && "<" == right) {
                return "X"
            }
        }

        return ""
    }

    private fun determineSmushes(): MutableMap<String, String> {
        val smushes = mutableMapOf<String, String>()
        if ((font.smushMode and SM_LOWLINE) != 0) {
            smushes.put("_", "|/\\\\[]{}()<>")
        }

        if ((font.smushMode and SM_HIERARCHY) != 0) {
            smushes.putAll(
                mapOf(
                    "|" to "|/\\\\[]{}()<>",
                    "\\\\/" to "[]{}()<>",
                    "[]" to "{}()<>",
                    "{}" to "()<>",
                    "()" to "<>"
                )
            )
        }
        return smushes
    }

    private fun ltrim(s: String): String = s.replace("^\\s+".toRegex(), "")

    private fun rtrim(s: String): String = s.replace("\\s+$".toRegex(), "")
}
