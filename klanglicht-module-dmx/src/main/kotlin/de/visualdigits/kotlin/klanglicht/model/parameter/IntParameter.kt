package de.visualdigits.kotlin.klanglicht.model.parameter

class IntParameter(
    val name: String,
    var value: Int = 0
) : Parameter<IntParameter> {

    override fun parameterMap(): Map<String, Int> = mapOf(name to value)

    override fun fade(other: Any, factor: Double): IntParameter {
        return if (other is IntParameter) {
            if (name != other.name) throw IllegalArgumentException("Cannot fade different parameters")
            IntParameter(name, ((value + (other.value - value) * factor).toInt()))
        } else throw IllegalArgumentException("Cannot not fade another type")
    }
}
