package de.visualdigits.kotlin.minim.audio

import de.visualdigits.kotlin.minim.buffer.DoubleSampleBuffer
import de.visualdigits.kotlin.minim.buffer.MultiChannelBuffer
import org.slf4j.LoggerFactory
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control
import javax.sound.sampled.SourceDataLine


class JSAudioOutput(
    sdl: SourceDataLine,
    private val bufferSize: Int
) : Thread(), AudioOutput {

    private val log = LoggerFactory.getLogger(JSAudioOutput::class.java)

    private val audioFormat: AudioFormat= sdl.format
    private val buffer: MultiChannelBuffer = MultiChannelBuffer(bufferSize, audioFormat.channels)
    private var listener: AudioListener? = null
    private var stream: AudioStream? = null
    private var running: Boolean = false

    private val doubleSampleBuffer: DoubleSampleBuffer = DoubleSampleBuffer(
        sampleCount = bufferSize,
        channelCount = audioFormat.channels,
        sampleRate = audioFormat.sampleRate.toDouble()
    )
    private var line: SourceDataLine? = sdl
    private val outBytes: ByteArray = ByteArray(doubleSampleBuffer.getByteArrayBufferSize(audioFormat))

    override fun run() {
        running = true
        line!!.start()
        while (running) {
            doubleSampleBuffer.makeSilence()
            readStream()
            if (line!!.format.channels == 1) {
                listener!!.samples(doubleSampleBuffer.getChannel(0))
            }
            else {
                listener!!.samples(doubleSampleBuffer.getChannel(0), doubleSampleBuffer.getChannel(1))
            }
            doubleSampleBuffer.convertToByteArray(outBytes, 0, audioFormat)
            if (line!!.available() == line!!.bufferSize) {
                log.debug("Likely buffer underrun in AudioOutput.")
            }
            line!!.write(outBytes, 0, outBytes.size)
            try {
                sleep(1)
            } catch (e: InterruptedException) {
                // ignore
            }
        }
        line!!.drain()
        line!!.stop()
        line!!.close()
        line = null
    }

    private fun readStream() {
        stream!!.read(buffer)
        for (i in 0 until buffer.getChannelCount()) {
            System.arraycopy(buffer.getChannel(i), 0, doubleSampleBuffer.getChannel(i), 0, doubleSampleBuffer.sampleCount)
        }
    }

    override fun open() {
        start()
    }

    override fun close() {
        running = false
    }

    override fun bufferSize(): Int {
        return bufferSize
    }

    override fun setAudioListener(listener: AudioListener) {
        this.listener = listener
    }

    override fun getControls(): Array<Control> {
        return line!!.controls
    }

    override fun setAudioStream(stream: AudioStream) {
        this.stream = stream
    }

    override fun getFormat(): AudioFormat = audioFormat
}

