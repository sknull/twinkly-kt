package de.visualdigits.kotlin.klanglicht.model.dmx

import com.google.common.primitives.Bytes
import org.apache.commons.lang3.StringUtils
import java.util.Arrays


class DmxFrame(
    var data: ByteArray = ByteArray(512)
) {

    companion object {
        private const val BYTE_MASK = 0xff
        private const val DMX_START: Byte = 0x7e
        private const val DMX_LABEL = 0x06.toByte()
        private const val DMX_DATALEN_LSB = 0x01.toByte()
        private const val DMX_DATALEN_HSB = 0x02.toByte()
        private const val DMX_PAUSE = 0x00.toByte()
        private const val DMX_STOP = 0xe7.toByte()

        private val header = byteArrayOf(
            DMX_START,
            DMX_LABEL,
            DMX_DATALEN_LSB,
            DMX_DATALEN_HSB,
            DMX_PAUSE
        )

        private val footer = byteArrayOf(
            DMX_STOP
        )
    }

    override fun toString(): String {
        return "DmxFrame [${dump()}]"
    }

    /**
     * Returns the bytes for a complete frame including header and footer.
     */
    fun getFrameBytes(): ByteArray = Bytes.concat(header, data, footer)

    /**
     * Sets all data bytes to 0.
     */
    fun init() {
        data.fill(0)
    }

    fun get(channel: Int): Int {
        return data[channel - 1].toInt() and BYTE_MASK
    }

    fun set(baseChannel: Int, bytes: ByteArray) {
        bytes.copyInto(data, baseChannel - 1)
    }

    fun dump(): String {
        val l: MutableList<String> = ArrayList()
        for (b in data) {
            l.add("0x" + StringUtils.leftPad(Integer.toHexString(b.toInt() and 0xff), 2, '0'))
        }
        return StringUtils.join(l, ", ")
    }

    fun clear() {
        Arrays.fill(data, 0.toByte())
    }
}

