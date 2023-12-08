package de.visualdigits.kotlin.util

import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

class ImageUtilTest {

    @Test
    fun testGamma() {
        val image = ImageIO.read(ClassLoader.getSystemResource("images/pacman/pacman_000.png"))
        val correctedImage = ImageUtil.applyGamma(image, 0.0001)
        ImageIO.write(correctedImage, "png", File("target/pacman_000_gamma.png"))
    }
}