package de.visualdigits.kotlin.twinkly.model.device.xmusic

import de.visualdigits.kotlin.twinkly.model.device.Session
import de.visualdigits.kotlin.twinkly.model.device.UDP_PORT_STREAMING
import de.visualdigits.kotlin.twinkly.model.device.xled.response.DeviceInfo
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MicEnabled
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MusicConfig
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MusicStats
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.musicmode.MusicMode
import de.visualdigits.kotlin.udp.UdpClient

/**
 * Specific session for the twinkly music device which is used
 * as networked microfone for audio input.
 */
class XMusic(
    ipAddress: String
): Session(
    ipAddress,
    "http://$ipAddress/xmusic/v1"
) {

    val deviceInfo: DeviceInfo?

    init {
        // ensure we are logged in up to here to avoid unneeded requests
        if (!tokens.containsKey(ipAddress)) login()
        if (tokens[ipAddress]?.loggedIn == true) {
            deviceInfo = get<DeviceInfo>("$baseUrl/gestalt")
        } else {
            deviceInfo = null
        }
    }

    fun musicConfig(): MusicConfig? {
        val response = get<MusicConfig>(
            url = "$baseUrl/music/config",
        )
        return response
    }

    fun musicMode(): MusicMode? {
        val response = get<MusicMode>(
            url = "$baseUrl/music/mode",
        )
        return response
    }

    fun musicMicEnabled(): MicEnabled? {
        val response = get<MicEnabled>(
            url = "$baseUrl/music/mic_enabled",
        )
        return response
    }

    fun musicStats(): MusicStats? {
        val response = get<MusicStats>(
            url = "$baseUrl/music/stats",
        )
        return response
    }

    fun readData(): String {
        val bytes = UdpClient(ipAddress, UDP_PORT_STREAMING).use { udpClient ->
            udpClient.read(1)
        }
        return String(bytes)
    }
}
