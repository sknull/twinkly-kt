package de.visualdigits.kotlin.twinkly.model.device.xled.response

import de.visualdigits.kotlin.util.compareTo

class Version(
    val major: Int,
    val minor: Int,
    val sub: Int
) : Comparable<Version> {

    private val versionParts: Triple<Int, Int, Int> = Triple(major, minor, sub)

    companion object {
        val UNKNOWN = Version("0.0.0")
    }

    constructor(version: String) : this(
        major = version.split('.')[0].toInt(),
        minor = version.split('.')[1].toInt(),
        sub = version.split('.')[2].toInt()
    )

    override fun toString(): String {
        return "$major.$minor.$sub"
    }

    override fun compareTo(other: Version): Int {
        return this.versionParts.compareTo(other.versionParts)
    }
}
