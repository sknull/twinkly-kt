package de.visualdigits.kotlin.twinkly.model.device.xmusic

import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.xled.AbstractXled
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedLayout
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.LedMovieConfigResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.moods.MoodsEffect
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MicEnabledResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MusicStatsResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.musicmode.Config
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.musicmode.MusicMode
import de.visualdigits.kotlin.udp.UdpClient

/**
 * Specific session for the twinkly music device which is used
 * as networked microfone for audio input.
 */
class XMusic private constructor(
    ipAddress: String
): AbstractXled(
    ipAddress = ipAddress,
    baseUrl = "http://$ipAddress/xmusic/v1"
) {

    companion object {

        private val cache = mutableMapOf<String, XMusic>()

        fun instance(
            ipAddress: String
        ): XMusic {
            return cache.computeIfAbsent(ipAddress) { XMusic(ipAddress) }
        }
    }

    override fun getLedLayoutResponse(): LedLayout? {
        return null
    }

    override fun getLedMovieConfigResponse(): LedMovieConfigResponse? {
        return null
    }

    fun getMusicMode(): MusicMode? {
        val response = get<MusicMode>(
            url = "$baseUrl/music/mode",
            clazz = MusicMode::class.java
        )
        return response
    }

    fun setMusicMode(musicMode: MusicMode): JsonObject? {
        val response = post<JsonObject>(
            url = "$baseUrl/music/mode",
            body = musicMode.writeValueAssBytes(),
            clazz = JsonObject::class.java
        )
        return response
    }

    fun setMoodsEffect(moodsEffect: MoodsEffect): JsonObject? {
        return setMusicMode(MusicMode(
            mode = "v2",
            config = Config(
                moodIndex = moodsEffect.moodIndex(),
                effectIndex = moodsEffect.index
            )
        ))
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
