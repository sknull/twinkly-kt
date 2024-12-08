package de.visualdigits.kotlin.twinkly.model.color

abstract class TwinklyColor<T : TwinklyColor<T>> : Color<T> {

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
                    val rgbColor = toRgbColor()
                    byteArrayOf(
                        rgbColor.red.toByte(),
                        rgbColor.green.toByte(),
                        rgbColor.blue.toByte()
                    )
                }
            }
            is RGBColor -> {
                if (bytesPerLed == 4) {
                    val rgbwColor = toRgbwColor()
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
            is HSVColor -> {
                if (bytesPerLed == 4) {
                    val rgbwColor = toRgbwColor()
                    byteArrayOf(
                        rgbwColor.white.toByte(),
                        rgbwColor.red.toByte(),
                        rgbwColor.green.toByte(),
                        rgbwColor.blue.toByte()
                    )
                }
                else {
                    val rgbColor = toRgbColor()
                    byteArrayOf(
                        rgbColor.red.toByte(),
                        rgbColor.green.toByte(),
                        rgbColor.blue.toByte()
                    )
                }
            }
            else -> byteArrayOf()
        }
    }
}
