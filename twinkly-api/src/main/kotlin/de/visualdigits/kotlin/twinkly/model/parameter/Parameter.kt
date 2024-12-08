package de.visualdigits.kotlin.twinkly.model.parameter

interface Parameter<T : Parameter<T>> : Fadeable<T> {

    fun parameterMap(): Map<String, Int>
}
