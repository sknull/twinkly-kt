package de.visualdigits.kotlin.minim.audio

import de.visualdigits.kotlin.minim.buffer.FloatSampleBuffer
import de.visualdigits.kotlin.minim.buffer.MultiChannelBuffer
import org.slf4j.LoggerFactory
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control
import javax.sound.sampled.TargetDataLine


class JSAudioInput(tdl: TargetDataLine, bufferSize: Int) : Thread(), AudioResource {

    private val log = LoggerFactory.getLogger(JSAudioInput::class.java)

    // line reading variables
    private var line: TargetDataLine?
    private val buffer: FloatSampleBuffer
    private val bufferSize: Int
    private var finished: Boolean
    private val rawBytes: ByteArray

    init {
        line = tdl
        this.bufferSize = bufferSize
        buffer = FloatSampleBuffer(
            sampleCount = bufferSize,
            channelCount = tdl.format.channels,
            sampleRate = tdl.format.sampleRate.toFloat()
        )
        finished = false
        val byteBufferSize = buffer.getByteArrayBufferSize(line!!.format)
        log.debug("byteBufferSize is $byteBufferSize")
        rawBytes = ByteArray(byteBufferSize)
    }

    override fun run() {
        line!!.start()
        while (!finished) {
            // read from the line
            line!!.read(rawBytes, 0, rawBytes.size)
            // convert to float samples
            buffer.setSamplesFromBytes(
                rawBytes, 0, line!!.format,
                0, buffer.sampleCount
            )
            // apply effects, if any, and broadcast the result
            // to all listeners
            try {
                sleep(10)
            } catch (e: InterruptedException) {
            }
        }
        // we are done, clean up the line
        line!!.flush()
        line!!.stop()
        line!!.close()
        line = null
    }

    override fun open() {
        // start();
        line!!.start()
    }

    override fun close() {
        finished = true
        // we are done, clean up the line
        line!!.flush()
        line!!.stop()
        line!!.close()
    }

    override fun getFormat(): AudioFormat {
        return line!!.format
    }

    override fun getControls(): Array<Control> {
        return line!!.controls
    }

    override fun read(buffer: MultiChannelBuffer): Int {
        // create our converter object
        val numChannels = line!!.format.channels
        val numSamples = buffer.bufferSize
        val sampleRate = line!!.format.sampleRate
        val convert = FloatSampleBuffer(
            sampleCount = numSamples,
            channelCount = numChannels,
            sampleRate = sampleRate.toFloat()
        )
        // allocate enough bytes for the size of this buffer
        val bytes = ByteArray(convert.getByteArrayBufferSize(line!!.format))
        // read the bytes
        line!!.read(bytes, 0, bytes.size)
        // convert the bytes
        convert.setSamplesFromBytes(bytes, 0, line!!.format, 0, numSamples)
        // copy the converted floats into the MultiChannelBuffer
        // make sure it has the correct number of channels first
        buffer.setChannelCount(numChannels)
        for (i in 0 until convert.channelCount) {
            buffer.setChannel(i, convert.getChannel(i))
        }
        return numSamples
    }
}

