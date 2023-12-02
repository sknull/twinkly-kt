package de.visualdigits.kotlin.minim

import javax.sound.sampled.Line


/**
 * A mixer is an audio device with one or more lines. It need not be designed
 * for mixing audio signals. A mixer that actually mixes audio has multiple
 * input (source) lines and at least one output (target) line. The former are
 * often instances of classes that implement [SourceDataLine], and the
 * latter, [TargetDataLine]. [Port] objects, too, are either source
 * lines or target lines. A mixer can accept prerecorded, loopable sound as
 * input, by having some of its source lines be instances of objects that
 * implement the [Clip] interface.
 *
 *
 * Through methods of the `Line` interface, which `Mixer` extends, a
 * mixer might provide a set of controls that are global to the mixer. For
 * example, the mixer can have a master gain control. These global controls are
 * distinct from the controls belonging to each of the mixer's individual lines.
 *
 *
 * Some mixers, especially those with internal digital mixing capabilities, may
 * provide additional capabilities by implementing the `DataLine`
 * interface.
 *
 *
 * A mixer can support synchronization of its lines. When one line in a
 * synchronized group is started or stopped, the other lines in the group
 * automatically start or stop simultaneously with the explicitly affected one.
 *
 * @author Kara Kytle
 * @since 1.3
 */
interface Mixer : Line {
    /**
     * Obtains information about this mixer, including the product's name,
     * version, vendor, etc.
     *
     * @return a mixer info object that describes this mixer
     * @see MixerInfo
     */
    val mixerInfo: MixerInfo?

    /**
     * Obtains information about the set of source lines supported by this
     * mixer. Some source lines may only be available when this mixer is open.
     *
     * @return array of `Line.Info` objects representing source lines for
     * this mixer. If no source lines are supported, an array of length
     * 0 is returned.
     */
    val sourceLineLineInfo: Array<Line.Info>?

    /**
     * Obtains information about the set of target lines supported by this
     * mixer. Some target lines may only be available when this mixer is open.
     *
     * @return array of `Line.Info` objects representing target lines for
     * this mixer. If no target lines are supported, an array of length
     * 0 is returned.
     */
    val targetLineLineInfo: Array<Line.Info>?

    /**
     * Obtains information about source lines of a particular type supported by
     * the mixer. Some source lines may only be available when this mixer is
     * open.
     *
     * @param  lineInfo a `Line.Info` object describing lines about which
     * information is queried
     * @return an array of `Line.Info` objects describing source lines
     * matching the type requested. If no matching source lines are
     * supported, an array of length 0 is returned.
     */
    fun getSourceLineInfo(lineInfo: Line.Info): Array<Line.Info>?

    /**
     * Obtains information about target lines of a particular type supported by
     * the mixer. Some target lines may only be available when this mixer is
     * open.
     *
     * @param  lineInfo a `Line.Info` object describing lines about which
     * information is queried
     * @return an array of `Line.Info` objects describing target lines
     * matching the type requested. If no matching target lines are
     * supported, an array of length 0 is returned.
     */
    fun getTargetLineInfo(lineInfo: Line.Info): Array<Line.Info>

    /**
     * Indicates whether the mixer supports a line (or lines) that match the
     * specified `Line.Info` object. Some lines may only be supported when
     * this mixer is open.
     *
     * @param  lineInfo describes the line for which support is queried
     * @return `true` if at least one matching line is supported,
     * `false` otherwise
     */
    fun isLineSupported(lineInfo: Line.Info): Boolean

    /**
     * Obtains a line that is available for use and that matches the description
     * in the specified `Line.Info` object.
     *
     *
     * If a `DataLine` is requested, and `info` is an instance of
     * `DataLine.Info` specifying at least one fully qualified audio
     * format, the last one will be used as the default format of the returned
     * `DataLine`.
     *
     * @param  lineInfo describes the desired line
     * @return a line that is available for use and that matches the description
     * in the specified `Line.Info` object
     * @throws LineUnavailableException if a matching line is not available due
     * to resource restrictions
     * @throws IllegalArgumentException if this mixer does not support any lines
     * matching the description
     * @throws SecurityException if a matching line is not available due to
     * security restrictions
     */
    fun getLine(lineInfo: Line.Info): Line
    //$$fb 2002-04-12: fix for 4667258: behavior of Mixer.getMaxLines(Line.Info) method doesn't match the spec
    /**
     * Obtains the approximate maximum number of lines of the requested type
     * that can be open simultaneously on the mixer.
     *
     *
     * Certain types of mixers do not have a hard bound and may allow opening
     * more lines. Since certain lines are a shared resource, a mixer may not be
     * able to open the maximum number of lines if another process has opened
     * lines of this mixer.
     *
     *
     * The requested type is any line that matches the description in the
     * provided `Line.Info` object. For example, if the info object
     * represents a speaker port, and the mixer supports exactly one speaker
     * port, this method should return 1. If the info object represents a source
     * data line and the mixer supports the use of 32 source data lines
     * simultaneously, the return value should be 32. If there is no limit, this
     * function returns [AudioSystem.NOT_SPECIFIED].
     *
     * @param  lineInfo a `Line.Info` that describes the line for which the
     * number of supported instances is queried
     * @return the maximum number of matching lines supported, or
     * `AudioSystem.NOT_SPECIFIED`
     */
    fun getMaxLines(lineInfo: Line.Info): Int

    /**
     * Obtains the set of all source lines currently open to this mixer.
     *
     * @return the source lines currently open to the mixer. If no source lines
     * are currently open to this mixer, an array of length 0 is
     * returned.
     * @throws SecurityException if the matching lines are not available due to
     * security restrictions
     */
    val sourceLines: Array<Line?>?

    /**
     * Obtains the set of all target lines currently open from this mixer.
     *
     * @return target lines currently open from the mixer. If no target lines
     * are currently open from this mixer, an array of length 0 is
     * returned.
     * @throws SecurityException if the matching lines are not available due to
     * security restrictions
     */
    val targetLines: Array<Line?>?

    /**
     * Synchronizes two or more lines. Any subsequent command that starts or
     * stops audio playback or capture for one of these lines will exert the
     * same effect on the other lines in the group, so that they start or stop
     * playing or capturing data simultaneously.
     *
     * @param  lines the lines that should be synchronized
     * @param  maintainSync `true` if the synchronization must be
     * precisely maintained (i.e., the synchronization must be
     * sample-accurate) at all times during operation of the lines, or
     * `false` if precise synchronization is required only during
     * start and stop operations
     * @throws IllegalArgumentException if the lines cannot be synchronized.
     * This may occur if the lines are of different types or have
     * different formats for which this mixer does not support
     * synchronization, or if all lines specified do not belong to this
     * mixer.
     */
    fun synchronize(lines: Array<Line?>?, maintainSync: Boolean)

    /**
     * Releases synchronization for the specified lines. The array must be
     * identical to one for which synchronization has already been established;
     * otherwise an exception may be thrown. However, `null` may be
     * specified, in which case all currently synchronized lines that belong to
     * this mixer are unsynchronized.
     *
     * @param  lines the synchronized lines for which synchronization should be
     * released, or `null` for all this mixer's synchronized lines
     * @throws IllegalArgumentException if the lines cannot be unsynchronized.
     * This may occur if the argument specified does not exactly match a
     * set of lines for which synchronization has already been
     * established.
     */
    fun unsynchronize(lines: Array<Line?>?)

    /**
     * Reports whether this mixer supports synchronization of the specified set
     * of lines.
     *
     * @param  lines the set of lines for which synchronization support is
     * queried
     * @param  maintainSync `true` if the synchronization must be
     * precisely maintained (i.e., the synchronization must be
     * sample-accurate) at all times during operation of the lines, or
     * `false` if precise synchronization is required only during
     * start and stop operations
     * @return `true` if the lines can be synchronized, `false`
     * otherwise
     */
    fun isSynchronizationSupported(lines: Array<Line?>?, maintainSync: Boolean): Boolean

}

