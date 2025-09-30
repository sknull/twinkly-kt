package de.visualdigits.kotlin.twinkly.model.color

/**
 * Determines how a color having more channels than the converted one
 * is treated regarding the additianal channel.
 */
enum class NormalizeMode {

    /**
     * The source color is just copied over and the additional channel left alone.
     * RGB to RGBW or RGBA will end up with the source values and W / A set to 0.
     */
    NONE,

    /**
     * The source color values are copied over and the additional channel will
     * be set to the minimum value of R, G and B then the additional value will
     * be subtracted from the original values (under cover removal).
     */
    STANDARD,

    /**
     * The source color is taken over as if set to none.
     * Only when the color represents the full value (255) of the additional channel
     * it will be set to 0, 0, 0, 255.
     * This mode is meant for setups with mixed types of fixtures and will lead to
     * more consistent colors and still benefit from the additional channel to express
     * the full value (white or amber).
     */
    FULL_ONLY
}