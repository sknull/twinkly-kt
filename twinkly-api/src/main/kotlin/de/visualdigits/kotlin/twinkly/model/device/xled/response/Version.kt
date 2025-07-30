package de.visualdigits.kotlin.twinkly.model.device.xled.response

import de.visualdigits.kotlin.util.compareTo

class Version(
    major: Int,
    minor: Int,
    sub: Int
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

    override fun compareTo(other: Version): Int {
        return this.versionParts.compareTo(other.versionParts)
    }
}
