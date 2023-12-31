package de.visualdigits.kotlin.minim.audio

import de.visualdigits.kotlin.minim.buffer.FloatSampleBuffer
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

    private val buffer: FloatSampleBuffer = FloatSampleBuffer(
        sampleCount = bufferSize,
        channelCnt = getFormat().channels
    )
    private var listener: AudioListener? = null
    private var stream: AudioResource? = null
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
            if (samplesRead != buffer.sampleCount) {
                for (i in samplesRead until buffer.sampleCount) {
                    for (c in 0 until buffer.channelCnt) {
                        buffer.setSample(c, i, 0.0F)
                        buffer.setSample(c, i, 0.0F)
                    }
                }
            }
            if (buffer.channelCnt == 1) {
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

    override fun read(buffer: FloatSampleBuffer): Int = 0

    override fun bufferSize(): Int {
        return buffer.sampleCount
    }

    override fun setAudioStream(stream: AudioResource) {
        this.stream = stream
    }

    override fun setAudioListener(listener: AudioListener) {
        this.listener = listener
    }
}

