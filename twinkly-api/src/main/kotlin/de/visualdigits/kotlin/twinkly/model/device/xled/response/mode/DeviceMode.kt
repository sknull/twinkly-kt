package de.visualdigits.kotlin.twinkly.model.device.xled.response.mode

enum class DeviceMode {

    /** plays predefined or uploaded effect. If movie hasn’t been set (yet) code 1104 is returned. */
    movie,

    /** plays a movie from a playlist. Since firmware version 2.5.6. */
    playlist,

    /** receive effect in real time */
    rt,

    /** starts predefined sequence of effects that are changed after few seconds */
    demo,

    /** plays effect with effect_id */
    effect,

    /** shows a static color */
    color,

    /** turns off lights */
    off
}
