package de.visualdigits.kotlin.twinkly.model.device.xled

enum class DeviceOrigin {

    TOP_LEFT, // for a curtain 210 this would be portrait upright
    TOP_RIGHT, // for a curtain 210 this would be landscape (turned right)
    BOTTOM_LEFT, // for a curtain 210 this would be landscape (turned left)
    BOTTOM_RIGHT // for a curtain 210 this would be portrait upside down
    ;

    fun isPortrait(): Boolean {
        return this == TOP_LEFT || this == BOTTOM_RIGHT
    }

    fun isLandscape(): Boolean {
        return this == TOP_RIGHT || this == BOTTOM_LEFT
    }
}
