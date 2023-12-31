package de.visualdigits.kotlin.minim

import de.visualdigits.kotlin.minim.audio.AudioInput
import de.visualdigits.kotlin.minim.audio.AudioInputType
import de.visualdigits.kotlin.minim.audio.AudioOutput
import de.visualdigits.kotlin.minim.audio.AudioResource
import de.visualdigits.kotlin.minim.audio.BasicAudioOutput
import de.visualdigits.kotlin.minim.audio.JSAudioInput
import de.visualdigits.kotlin.minim.audio.JSAudioOutput
import org.slf4j.LoggerFactory
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

class Minim {

    private val log = LoggerFactory.getLogger(Minim::class.java)

    /**
     * An AudioInput is used when you want to monitor the active audio input
     * of the computer. On a laptop, for instance, this will typically be
     * the built-in microphone. On a desktop it might be the line-in
     * port on the soundcard. The default values are for a stereo input
     * with a 1024 sample buffer (ie the size of left, right, and mix
     * buffers), sample rate of 44100 and bit depth of 16. Generally
     * speaking, you will not want to specify these things, but it's
     * there if you need it.
     *
     * @return an AudioInput that reads from the active audio input of the soundcard
     * @see .getLineIn
     */
    fun getLineIn(): AudioInput? {
        return getLineIn(AudioInputType.STEREO)
    }

    /**
     * Gets either a MONO or STEREO [AudioInput].
     *
     * @param type Minim.MONO or Minim.STEREO
     * @return an `AudioInput` with the requested type, a 1024 sample
     * buffer, a sample rate of 44100 and a bit depth of 16
     * @see .getLineIn
     */
    fun getLineIn(type: AudioInputType): AudioInput? {
        return getLineIn(type, 1024, 44100f, 16)
    }

    /**
     * Gets an [AudioInput].
     *
     * @param type       Minim.MONO or Minim.STEREO
     * @param bufferSize int: how long you want the `AudioInput`'s sample buffer
     * to be (ie the size of left, right, and mix buffers)
     * @param sampleRate float: the desired sample rate in Hertz (typically 44100)
     * @param bitDepth   int: the desired bit depth (typically 16)
     * @return an `AudioInput` with the requested attributes
     */
    private fun getLineIn(type: AudioInputType, bufferSize: Int, sampleRate: Float, bitDepth: Int): AudioInput? {
        var input: AudioInput? = null
        val stream: AudioResource? = getAudioInput(type, bufferSize, sampleRate, bitDepth)
        if (stream != null) {
            var out: AudioOutput? = getAudioOutput(type.channels, bufferSize, sampleRate, bitDepth)
            // couldn't get an output, the system might not have one available
            // so in that case we provide a basic audio out to the input
            // that will pull samples from it and so forth
            if (out == null) {
                out = BasicAudioOutput(stream.getFormat(), bufferSize)
            }
            input = AudioInput(stream, out)
        }
        return input
    }

    /**
     * Gets an [AudioInput].
     *
     * @param type       Minim.MONO or Minim.STEREO
     * @param bufferSize int: how long you want the `AudioInput`'s sample buffer
     * to be (ie the size of left, right, and mix buffers)
     * @return an `AudioInput` with the requested attributes, a
     * sample rate of 44100 and a bit depth of 16
     * @see .getLineIn
     */
    fun getLineIn(type: AudioInputType, bufferSize: Int): AudioInput? {
        return getLineIn(type, bufferSize, 44100f, 16)
    }

    /**
     * Gets an [AudioInput].
     *
     * @param type       Minim.MONO or Minim.STEREO
     * @param bufferSize int: how long you want the `AudioInput`'s sample buffer
     * to be (ie the size of left, right, and mix buffers)
     * @param sampleRate float: the desired sample rate in Hertz (typically 44100)
     * @return an `AudioInput` with the requested attributes and a
     * bit depth of 16
     * @see .getLineIn
     */
    fun getLineIn(type: AudioInputType, bufferSize: Int, sampleRate: Float): AudioInput? {
        return getLineIn(type, bufferSize, sampleRate, 16)
    }

    private fun getAudioInput(
        type: AudioInputType, bufferSize: Int,
        sampleRate: Float, bitDepth: Int
    ): AudioResource? {
        require(!(bitDepth != 8 && bitDepth != 16)) { "Unsupported bit depth, use either 8 or 16." }
        val format = AudioFormat(sampleRate, bitDepth, type.channels, true, false)
        val line: TargetDataLine? = getTargetDataLine(format, bufferSize * 4)
        return if (line != null) {
            JSAudioInput(line, bufferSize)
        }
        else null
    }

    private fun getTargetDataLine(format: AudioFormat, bufferSize: Int): TargetDataLine? {
        var line: TargetDataLine? = null
        val dataLineInfo = DataLine.Info(TargetDataLine::class.java, format)
        if (AudioSystem.isLineSupported(dataLineInfo)) {
            try {
                line = AudioSystem.getLine(dataLineInfo) as TargetDataLine
                line.open(format, bufferSize * format.frameSize)
            } catch (e: Exception) {
                log.error("Error acquiring TargetDataLine: " + e.message)
            }
        }
        else {
            log.error("Unable to return a TargetDataLine: unsupported format - $format")
        }
        return line
    }

    private fun getAudioOutput(channels: Int, bufferSize: Int, sampleRate: Float, bitDepth: Int): AudioOutput? {
        require(!(bitDepth != 8 && bitDepth != 16)) { "Unsupported bit depth, use either 8 or 16." }
        val format = AudioFormat(sampleRate, bitDepth, channels, true, false)
        val sdl: SourceDataLine? = getSourceDataLine(format, bufferSize)
        return if (sdl != null) {
            JSAudioOutput(sdl, bufferSize)
        }
        else null
    }

    private fun getSourceDataLine(format: AudioFormat, bufferSize: Int): SourceDataLine? {
        var line: SourceDataLine? = null
        val info = DataLine.Info(SourceDataLine::class.java, format)
        if (AudioSystem.isLineSupported(info)) {
            try {
                line = AudioSystem.getLine(info) as SourceDataLine
                line.open(format, bufferSize * format.frameSize * 4)
            } catch (e: java.lang.Exception) {
                log.error("Couldn't open the line: " + e.message)
            }
        }
        else {
            log.error("Unable to return a SourceDataLine: unsupported format - $format")
        }
        return line
    }
}
