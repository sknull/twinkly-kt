package de.visualdigits.kotlin.klanglicht.model.dmx


class DmxInterfaceDummy : DmxInterface() {

    override fun toString(): String {
        return repr()
    }

    override fun open(portName: String) {
        println("### open")
    }

    override fun close() {
        println("### close")
    }

    override fun write() {
        println("### write frame: ${dmxFrame.dump()}")
    }

    override fun read(): ByteArray {
        return ByteArray(0)
    }

    override fun write(data: ByteArray) {}

    override fun isOpen(): Boolean = true
}

