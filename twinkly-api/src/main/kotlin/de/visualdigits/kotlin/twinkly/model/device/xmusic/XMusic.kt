package de.visualdigits.kotlin.twinkly.model.device.xmusic

import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.Session
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MicEnabledResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MusicConfigResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MusicStatsResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.musicmode.MusicModeResponse
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

    fun getMusicConfig(): MusicConfigResponse? {
        val response = get<MusicConfigResponse>(
            url = "$baseUrl/music/config",
            clazz = MusicConfigResponse::class.java
        )
        return response
    }

    fun setMusicConfig(musicConfig: MusicConfigResponse): JsonObject? {
        val response = post<JsonObject>(
            url = "$baseUrl/music/config",
            body = musicConfig.writeValueAssBytes(),
            clazz = JsonObject::class.java
        )
        return response
    }

    fun getMusicMode(): MusicModeResponse? {
        val response = get<MusicModeResponse>(
            url = "$baseUrl/music/mode",
            clazz = MusicModeResponse::class.java
        )
        return response
    }

    fun isMusicMicEnabled(): MicEnabledResponse? {
        val response = get<MicEnabledResponse>(
            url = "$baseUrl/music/mic_enabled",
            clazz = MicEnabledResponse::class.java
        )
        return response
    }

    fun getMusicStats(): MusicStatsResponse? {
        val response = get<MusicStatsResponse>(
            url = "$baseUrl/music/stats",
            clazz = MusicStatsResponse::class.java
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
