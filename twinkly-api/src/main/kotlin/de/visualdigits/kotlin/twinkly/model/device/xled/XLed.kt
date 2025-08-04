package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.common.networkstatus.NetworkStatus
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedLayout
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.LedMode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.LedMovieConfigResponse
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

/**
 * Base class for specific twinkly devices.
 * Handles login and out.
 */
interface XLed {

    var width: Int
    var height: Int

    companion object {

        const val UDP_PORT_DISCOVER = 5555 // scraped from elsewhere...
        const val UDP_PORT_MUSIC = 5556 // scraped from elsewhere...

        fun discoverTwinklyDevices(timeoutMillis: Int = 2000): List<String> {
            val devices = mutableListOf<String>()
            val socket = DatagramSocket()
            socket.broadcast = true
            socket.soTimeout = timeoutMillis

            // Twinkly devices respond to this "discover" message
            val data = ByteArray(1) + "discover".toByteArray()
            val packet = DatagramPacket(
                data,
                data.size,
                InetAddress.getByName("255.255.255.255"),
                UDP_PORT_DISCOVER
            )

            // Send broadcast
            socket.send(packet)

            val buffer = ByteArray(1024)
            val receivePacket = DatagramPacket(buffer, buffer.size)
            val stopTime = System.currentTimeMillis() + timeoutMillis
            while (System.currentTimeMillis() < stopTime) {
                try {
                    socket.receive(receivePacket)
                    String(receivePacket.data, 0, receivePacket.length)
                    devices.add(receivePacket.address.hostAddress)
                } catch (_: SocketTimeoutException) {
                    break
                }
            }
            socket.close()

            return devices.distinct()
        }
    }

    fun getLedLayoutResponse(): LedLayout?

    fun getLedMovieConfigResponse(): LedMovieConfigResponse?

    /**
     * Performs the challenge response sequence needed to actually log in to the device.
     */
    fun login()

    /**
     * The current token expires after a device chosen time and has then to be refreshed.
     * Current token is removed and a new login is performed.
     */
    fun refreshTokenIfNeeded()

    /**
     * Determines if the session is logged into the device.
     */
    fun isLoggedIn(): Boolean

    /**
     * Logs the session out of the device.
     */
    fun logout()

    fun getLedMode(): LedMode?

    fun setLedMode(ledMode: LedMode): JsonObject?

    fun showRealTimeFrame(frame: XledFrame)

    fun showRealTimeSequence(
        frameSequence: XledSequence,
        loop: Int
    )
    /**
     * Returns the devices current status.
     */
    fun getStatus(): JsonObject?

    /**
     * Returns infos about the network configuration of the device.
     */
    fun getNetworkStatus(): NetworkStatus?

    fun getEndpoint(uri: String): String?

    fun getEndpointRaw(uri: String): String?
}
