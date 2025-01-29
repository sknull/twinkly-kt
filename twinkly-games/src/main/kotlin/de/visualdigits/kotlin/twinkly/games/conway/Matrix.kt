package de.visualdigits.kotlin.twinkly.games.conway

class Matrix(
    val width: Int,
    val height: Int,
    val matrix: MutableList<MutableList<Int>> = mutableListOf()
): MutableList<MutableList<Int>> by matrix {

    init {
        for (x in 0 until width) {
            val row = mutableListOf<Int>()
            for (y in 0 until height) {
                row.add(0)
            }
            matrix.add(row)
        }
    }

    override fun toString(): String {
        val s = mutableListOf<String>()
        for (y in 0 until height) {
            val row = mutableListOf<Int>()
            for (x in 0 until width) {
                row.add(matrix[x][y])
            }
            s.add(row.joinToString(""))
        }
        return s.joinToString("\n")
    }

    fun clone(): Matrix {
        val clone = Matrix(width, height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                clone[x][y] = matrix[x][y]
            }
        }
        return clone
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Matrix

        return matrix == other.matrix
    }

    override fun hashCode(): Int {
        return matrix.hashCode()
    }
}
