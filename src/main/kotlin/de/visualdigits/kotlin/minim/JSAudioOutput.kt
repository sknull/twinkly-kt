package de.visualdigits.kotlin.minim

import org.slf4j.LoggerFactory
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control
import javax.sound.sampled.SourceDataLine


class JSAudioOutput(sdl: SourceDataLine, private val bufferSize: Int) : Thread(), AudioOut {

    private val log = LoggerFactory.getLogger(JSAudioOutput::class.java)

    private var listener: AudioListener? = null
    private var stream: AudioStream? = null
    private var line: SourceDataLine?

    private val audioFormat: AudioFormat
    private val buffer: DoubleSampleBuffer
    private val mcBuffer: MultiChannelBuffer
    private var finished: Boolean
    private val outBytes: ByteArray

    init {
        audioFormat = sdl.format
        buffer = DoubleSampleBuffer(
            sampleCount = bufferSize,
            channelCount = audioFormat.channels,
            sampleRate = audioFormat.sampleRate.toDouble()
        )
        mcBuffer = MultiChannelBuffer(bufferSize, audioFormat.channels)
        outBytes = ByteArray(buffer.getByteArrayBufferSize(audioFormat))
        finished = false
        line = sdl
    }

    override fun run() {
        line!!.start()
        while (!finished) {
            buffer.makeSilence()
            readStream()
            if (line!!.format.channels == 1) {
                listener!!.samples(buffer.getChannel(0))
            }
            else {
                listener!!.samples(buffer.getChannel(0), buffer.getChannel(1))
            }
            buffer.convertToByteArray(outBytes, 0, audioFormat)
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
        stream!!.read(mcBuffer)
        for (i in 0 until mcBuffer.getChannelCount()) {
            System.arraycopy(mcBuffer.getChannel(i), 0, buffer.getChannel(i), 0, buffer.sampleCount)
        }
    }

    override fun open() {
        start()
    }

    override fun close() {
        finished = true
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

