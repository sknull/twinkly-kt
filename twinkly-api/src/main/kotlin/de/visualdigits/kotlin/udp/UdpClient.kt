package de.visualdigits.kotlin.udp

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpClient(
    host: String,
    val port: Int
) : AutoCloseable {

    private val socket: DatagramSocket = DatagramSocket()
    private val address: InetAddress = InetAddress.getByName(host)

    fun send(bytes: ByteArray) {
        socket.send(DatagramPacket(bytes, bytes.size, address, port))
    }

    fun read(numBytes: Int): ByteArray {
        val buffer = ByteArray(numBytes)
        val packet = DatagramPacket(buffer, buffer.size, address, port)
        socket.receive(packet)
        return packet.data
    }

    override fun close() {
        socket.close()
    }
}
