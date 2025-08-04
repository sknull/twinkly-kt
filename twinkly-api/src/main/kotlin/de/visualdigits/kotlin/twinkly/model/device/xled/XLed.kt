package de.visualdigits.kotlin.twinkly.model.device.xled

import de.visualdigits.kotlin.twinkly.model.color.Color
import de.visualdigits.kotlin.twinkly.model.common.JsonObject
import de.visualdigits.kotlin.twinkly.model.device.xled.request.CurrentMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.request.NewMovieRequest
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Brightness
import de.visualdigits.kotlin.twinkly.model.device.xled.response.PlayList
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Saturation
import de.visualdigits.kotlin.twinkly.model.device.xled.response.Timer
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.CurrentLedEffectResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedConfigResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.led.LedEffectsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.LedMode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.mode.Mode
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.CurrentMovieResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.LedMovieConfigResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.Movie
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.Movies
import de.visualdigits.kotlin.twinkly.model.device.xled.response.movie.NewMovieResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicDriverSetResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicDriversResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.CurrentMusicEffectResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.LedMusicStatsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicDriversSets
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicEffectsResponse
import de.visualdigits.kotlin.twinkly.model.device.xled.response.music.MusicEnabledResponse
import de.visualdigits.kotlin.twinkly.model.device.xmusic.response.MusicConfig
import de.visualdigits.kotlin.twinkly.model.playable.XledFrame
import de.visualdigits.kotlin.twinkly.model.playable.XledSequence
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.time.OffsetDateTime

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

    fun getIpAddress(): String

    fun getLedMovieConfig(): LedMovieConfigResponse?

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

    fun powerOn()

    fun powerOff()

    fun getMode(): Mode?

    fun getLedMode(): LedMode?

    fun setLedMode(ledMode: LedMode): JsonObject?

    fun ledReset()

    fun getMusicEffects(): MusicEffectsResponse?

    fun getCurrentMusicEffect(): CurrentMusicEffectResponse?

    fun setCurrentMusicEffect(effectId: String): JsonObject?

    fun getMusicConfig(): MusicConfig?

    fun getLedMusicStats(): LedMusicStatsResponse?

    fun getMusicEnabled(): MusicEnabledResponse?

    fun setMusicEnabled(enabled: Boolean): JsonObject?

    fun getMusicDriversCurrent(): CurrentMusicDriversResponse?

    fun getMusicDriversSets(): MusicDriversSets?

    fun getCurrentMusicDriversSet(): CurrentMusicDriverSetResponse?

    fun getBrightness(): Brightness?

    fun setBrightness(brightness: Float)

    fun getSaturation(): Saturation?

    fun setSaturation(saturation: Float)

    fun getColor(): Color<*>

    fun setColor(color: Color<*>)

    fun getLedConfig(): LedConfigResponse?

    fun getLedEffects(): LedEffectsResponse?

    fun getCurrentLedEffect(): CurrentLedEffectResponse?

    fun setCurrentLedEffect(effectId: String): JsonObject?

    fun getMovies(): Movies?

    fun deleteMovies(): JsonObject?

    fun getCurrentMovie(): CurrentMovieResponse?

    fun setCurrentMovie(currentMovieRequest: CurrentMovieRequest): JsonObject?

    fun getPlaylist(): PlayList?

    fun getCurrentPlaylist(): String?

    fun showFrame(
        name: String,
        frame: XledFrame
    )

    /**
     * Experimental code which tries to upload a new movie and plays it in device.
     * Seems to overwrite the current sequence which is active in the device.
     */
    fun showSequence(
        name: String,
        sequence: XledSequence,
        fps: Int
    )

    fun showRealTimeFrame(frame: XledFrame)

    fun showRealTimeSequence(
        frameSequence: XledSequence,
        loop: Int
    ) {
        val frames = frameSequence
            .filter { it is XledFrame }
            .map { it as XledFrame }

        var loopCount = loop
        while (loopCount == -1 || loopCount > 0) {
            frames.forEach { frame ->
                showRealTimeFrame(frame)
                Thread.sleep(frameSequence.frameDelay)
            }
            if (loopCount != -1) loopCount--
        }
    }

    fun setLedMovieConfig(movieConfig: LedMovieConfigResponse): JsonObject?

    fun uploadNewMovie(newMovie: NewMovieRequest): NewMovieResponse?

    fun uploadNewMovieToListOfMovies(frame: XledFrame): Movie?

    fun uploadNewMovieToListOfMovies(bytes: ByteArray): Movie?

    fun getTimer(): Timer?

    fun setTimer(
        timeOn: OffsetDateTime,
        timeOff: OffsetDateTime
    ): Timer?

    fun setTimer(
        timeOnHour: Int,
        timeOnMinute: Int,
        timeOffHour: Int,
        timeOffMinute: Int
    ): Timer?

    fun setTimer(timer: Timer): Timer?
}
