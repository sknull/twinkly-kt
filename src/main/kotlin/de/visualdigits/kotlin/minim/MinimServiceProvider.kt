package de.visualdigits.kotlin.minim

import org.slf4j.LoggerFactory
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine

class MinimServiceProvider {
    
    private val log = LoggerFactory.getLogger(MinimServiceProvider::class.java)

    private val inputMixer: Mixer? = null
    private val outputMixer: Mixer? = null

    fun getAudioInput(
        type: AudioInputType, bufferSize: Int,
        sampleRate: Float, bitDepth: Int
    ): AudioStream? {
        require(!(bitDepth != 8 && bitDepth != 16)) { "Unsupported bit depth, use either 8 or 16." }
        val format = AudioFormat(sampleRate, bitDepth, type.channels, true, false)
        val line: TargetDataLine? = getTargetDataLine(format, bufferSize * 4)
        return if (line != null) {
            JSAudioInput(line, bufferSize)
        }
        else null
    }

    fun getTargetDataLine(format: AudioFormat, bufferSize: Int): TargetDataLine? {
        var line: TargetDataLine? = null
        val dataLineInfo = DataLine.Info(TargetDataLine::class.java, format)
        if (AudioSystem.isLineSupported(dataLineInfo)) {
            try {
                line = if (inputMixer == null) {
                    AudioSystem.getLine(dataLineInfo) as TargetDataLine
                }
                else {
                    inputMixer.getLine(dataLineInfo) as TargetDataLine
                }
                line!!.open(format, bufferSize * format.frameSize)
                log.debug(
                    """
                    TargetDataLine buffer size is ${line!!.bufferSize}
                    TargetDataLine format is ${line!!.format}
                    TargetDataLine info is ${line!!.lineInfo}
                    """.trimIndent()
                )
            } catch (e: Exception) {
                log.error("Error acquiring TargetDataLine: " + e.message)
            }
        }
        else {
            log.error("Unable to return a TargetDataLine: unsupported format - $format")
        }
        return line
    }

    fun getAudioOutput(channels: Int, bufferSize: Int, sampleRate: Float, bitDepth: Int): AudioOut? {
        require(!(bitDepth != 8 && bitDepth != 16)) { "Unsupported bit depth, use either 8 or 16." }
        val format = AudioFormat(sampleRate, bitDepth, channels, true, false)
        val sdl: SourceDataLine? = getSourceDataLine(format, bufferSize)
        return if (sdl != null) {
            JSAudioOutput(sdl, bufferSize)
        }
        else null
    }

    fun getSourceDataLine(format: AudioFormat, bufferSize: Int): SourceDataLine? {
        var line: SourceDataLine? = null
        val info = DataLine.Info(SourceDataLine::class.java, format)
        if (AudioSystem.isLineSupported(info)) {
            try {
                line = if (outputMixer == null) {
                    AudioSystem.getLine(info) as SourceDataLine
                }
                else {
                    outputMixer.getLine(info) as SourceDataLine
                }
                // remember that time you spent, like, an entire afternoon fussing
                // with this buffer size to try to get the latency decent on Linux
                // Yah, don't fuss with this anymore, ok
                line!!.open(format, bufferSize * format.frameSize * 4)
                if (line!!.isOpen) {
                    log.debug(
                        """SourceDataLine is ${line!!.javaClass}
Buffer size is ${line!!.bufferSize} bytes.
Format is ${line!!.format}."""
                    )
                }
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
