package de.visualdigits.kotlin.twinkly.games.conway

data class ConwayResult(
    val matrix: Matrix,
    val life: Int = 0,
    val changes: Int = 1,
    val cycle: Int = 0,
    val generations: MutableList<Matrix> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConwayResult

        return matrix == other.matrix
    }

    override fun hashCode(): Int {
        return matrix.hashCode()
    }
}
