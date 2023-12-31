package de.visualdigits.kotlin.util

import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.pow


object ImageUtil {

     fun applyGamma(image: BufferedImage, gamma: Double): BufferedImage {
        val gammaLut = (0 until 256)
            .map { (255.0 * (it.toDouble() / 255.0).pow(1.0 / gamma)).toInt() }
            .toIntArray()
        val correctedImage = BufferedImage(image.width, image.height, image.type)
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val color = Color(image.getRGB(x, y), true)
                correctedImage.setRGB(x, y, (color.alpha shl 24) + (gammaLut[color.red] shl 16) + (gammaLut[color.green] shl 8) + gammaLut[color.blue])
            }
        }
        return correctedImage
    }
}