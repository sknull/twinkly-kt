package de.visualdigits.kotlin.minim

/**
 * The `Mixer.Info` class represents information about an audio mixer,
 * including the product's name, version, and vendor, along with a textual
 * description. This information may be retrieved through the
 * [getMixerInfo][Mixer.getMixerInfo] method of the `Mixer`
 * interface.
 *
 * @author Kara Kytle
 * @since 1.3
 */
class MixerInfo protected constructor(
    /**
     * Mixer name.
     */
    val name: String,
    /**
     * Mixer vendor.
     */
    var vendor: String, description: String, version: String
) {
    /**
     * Obtains the name of the mixer.
     *
     * @return a string that names the mixer
     */
    /**
     * Obtains the vendor of the mixer.
     *
     * @return a string that names the mixer's vendor
     */
    /**
     * Obtains the description of the mixer.
     *
     * @return a textual description of the mixer
     */
    /**
     * Mixer description.
     */
    val description: String
    /**
     * Obtains the version of the mixer.
     *
     * @return textual version information for the mixer
     */
    /**
     * Mixer version.
     */
    val version: String

    /**
     * Constructs a mixer's info object, passing it the given textual
     * information.
     *
     * @param  name the name of the mixer
     * @param  vendor the company who manufactures or creates the hardware
     * or software mixer
     * @param  description descriptive text about the mixer
     * @param  version version information for the mixer
     */
    init {
        vendor = vendor
        this.description = description
        this.version = version
    }

    /**
     * Indicates whether the specified object is equal to this info object,
     * returning `true` if the objects are the same.
     *
     * @param  obj the reference object with which to compare
     * @return `true` if the specified object is equal to this info
     * object; `false` otherwise
     */
    override fun equals(obj: Any?): Boolean {
        return super.equals(obj)
    }

    /**
     * Returns a hash code value for this info object.
     *
     * @return a hash code value for this info object
     */
    override fun hashCode(): Int {
        return super.hashCode()
    }

    /**
     * Returns a string representation of the info object.
     *
     * @return a string representation of the info object
     */
    override fun toString(): String {
        return String.format("%s, version %s", name, version)
    }
}
