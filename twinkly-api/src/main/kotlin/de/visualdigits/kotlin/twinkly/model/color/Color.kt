package de.visualdigits.kotlin.twinkly.model.color

interface Color<T : Color<T>> {

    fun value(): Long

    fun hex(): String

    fun web(): String

    fun ansiColor(): String

    fun toRGB(): RGBColor

    fun toHSV(): HSVColor

    fun toRGBW(): RGBWColor

    fun parameterMap(): Map<String, Int>

    fun clone(): T

    /**
     * Blends this color towards the given color according to its alpha value of the given color.
     */
    fun blend(other: Any, blendMode: BlendMode): T

    /**
     * Fades this instance towards the given instance using the given factor 0.0 .. 1.0.
     */
    fun fade(other: Any, factor: Double, blendMode: BlendMode): T

    fun isBlack(): Boolean

    fun toAwtColor(): java.awt.Color

    fun toLedPixel(bytesPerLed: Int): ByteArray {
        return when (this) {
            is RGBWColor -> {
                if (bytesPerLed == 4) {
                    byteArrayOf(
                        white.toByte(),
                        red.toByte(),
                        green.toByte(),
                        blue.toByte()
                    )
                }
                else {
                    val rgbColor = toRGB()
                    byteArrayOf(
                        rgbColor.red.toByte(),
                        rgbColor.green.toByte(),
                        rgbColor.blue.toByte()
                    )
                }
            }
            is RGBColor -> {
                if (bytesPerLed == 4) {
                    val rgbwColor = toRGBW()
                    byteArrayOf(
                        rgbwColor.white.toByte(),
                        rgbwColor.red.toByte(),
                        rgbwColor.green.toByte(),
                        rgbwColor.blue.toByte()
                    )
                }
                else {
                    byteArrayOf(
                        red.toByte(),
                        green.toByte(),
                        blue.toByte()
                    )
                }
            }
            else -> byteArrayOf()
        }
    }
}
