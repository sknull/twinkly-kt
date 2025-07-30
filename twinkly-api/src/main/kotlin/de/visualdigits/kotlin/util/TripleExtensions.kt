package de.visualdigits.kotlin.util

@Suppress("UNCHECKED_CAST")
operator fun <T> Triple<Comparable<T>, Comparable<T>, Comparable<T>>.compareTo(other: Triple<Comparable<T>, Comparable<T>, Comparable<T>>): Int {
    return toList()
        .zip(other.toList<Comparable<T>>())
        .map { e -> e.first.compareTo(e.second as T) }
        .dropWhile { e -> e == 0 }
        .first()
}
