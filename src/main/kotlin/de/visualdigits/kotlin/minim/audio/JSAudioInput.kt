package de.visualdigits.kotlin.minim.audio

import de.visualdigits.kotlin.minim.buffer.DoubleSampleBuffer
import de.visualdigits.kotlin.minim.buffer.MultiChannelBuffer
import org.slf4j.LoggerFactory
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.Control
import javax.sound.sampled.TargetDataLine


class JSAudioInput(tdl: TargetDataLine, bufferSize: Int) : Thread(), AudioStream {

    private val log = LoggerFactory.getLogger(JSAudioInput::class.java)

    // line reading variables
    private var line: TargetDataLine?
    private val buffer: DoubleSampleBuffer
    private val bufferSize: Int
    private var finished: Boolean
    private val rawBytes: ByteArray

    init {
        line = tdl
        this.bufferSize = bufferSize
        buffer = DoubleSampleBuffer(
            sampleCount = bufferSize,
            channelCount = tdl.format.channels,
            sampleRate = tdl.format.sampleRate.toDouble()
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

    override fun read(): DoubleArray {
        // TODO: this is sort of terrible, but will do for now. would be much better
        // to dig the conversion stuff out of FloatSampleBuffer and do this more directly
        val numSamples = 1
        // allocate enough bytes for one sample frame
        val bytes = ByteArray(line!!.format.frameSize)
        line!!.read(bytes, 0, bytes.size)
        buffer.setSamplesFromBytes(bytes, 0, line!!.format, 0, numSamples)
        // allocate enough floats for the number of channels
        val samples = DoubleArray(buffer.channelCount)
        for (i in samples.indices) {
            samples[i] = buffer.getChannel(i)?.get(0)?:0.0
        }
        return samples
    }

    override fun read(buffer: MultiChannelBuffer): Int {
        // create our converter object
        val numChannels = line!!.format.channels
        val numSamples = buffer.bufferSize
        val sampleRate = line!!.format.sampleRate
        val convert = DoubleSampleBuffer(
            sampleCount = numSamples,
            channelCount = numChannels,
            sampleRate = sampleRate.toDouble()
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

