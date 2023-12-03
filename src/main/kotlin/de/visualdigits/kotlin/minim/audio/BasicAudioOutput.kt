package de.visualdigits.kotlin.minim.audio

import de.visualdigits.kotlin.minim.buffer.MultiChannelBuffer
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control

// ddf (9/5/15): very very basic audio out implementation
//             : that is used when creating an AudioInput
//             : in the event that getLineOut does not return
//             : a usable audio out.
class BasicAudioOutput(
    private val audioFormat: AudioFormat,
    bufferSize: Int
) : Thread(), AudioOutput {

    private val buffer: MultiChannelBuffer = MultiChannelBuffer(bufferSize, getFormat().channels)
    private var listener: AudioListener? = null
    private var stream: AudioStream? = null
    private var running = false

    override fun run() {
        running = true
        while (running) {
            // this should block until we get a full buffer
            val samplesRead = stream!!.read(buffer)

            // but with JavaSound, at least, it might return without
            // a full buffer if the TargetDataLine the stream is reading from
            // is closed during a read, so in that case we simply
            // fill the rest of the buffer with silence
            if (samplesRead != buffer.bufferSize) {
                for (i in samplesRead until buffer.bufferSize) {
                    for (c in 0 until buffer.getChannelCount()) {
                        buffer.setSample(c, i, 0.0)
                        buffer.setSample(c, i, 0.0)
                    }
                }
            }
            if (buffer.getChannelCount() == 1) {
                listener!!.samples(buffer.getChannel(0))
            }
            else {
                listener!!.samples(buffer.getChannel(0), buffer.getChannel(1))
            }
            try {
                sleep(1)
            } catch (e: InterruptedException) {
                // ignore
            }
        }
    }

    override fun open() {
        start()
    }

    override fun close() {
        running = false
    }

    override fun getControls(): Array<Control> {
        return arrayOf()
    }

    override fun getFormat(): AudioFormat = audioFormat

    override fun bufferSize(): Int {
        return buffer.bufferSize
    }

    override fun setAudioStream(stream: AudioStream) {
        this.stream = stream
    }

    override fun setAudioListener(listener: AudioListener) {
        this.listener = listener
    }
}

