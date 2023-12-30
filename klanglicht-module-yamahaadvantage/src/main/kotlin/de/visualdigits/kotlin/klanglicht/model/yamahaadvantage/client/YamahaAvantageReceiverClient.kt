package de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.ResponseCode
import de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.SoundProgramList
import de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.deviceinfo.DeviceInfo
import java.net.URL


class YamahaAvantageReceiverClient(val yamahaReceiverUrl: String) {

    companion object {

        private val mapper = jacksonObjectMapper()

        private val mapPrograms: MutableMap<String, String> = HashMap()

        init {
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Standard"] = "standard"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Sci-Fi"] = "sci-fi"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Spectacle"] = "spectacle"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Adventure"] = "adventure"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Drama"] = "drama"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Sports"] = "sports"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Music Video"] = "music_video"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Action Game"] = "action_game"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Roleplaying Game"] = "roleplaying_game"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Mono Movie"] = "mono_movie"

            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["The Roxy Theatre"] = "roxy_theatre"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["The Bottom Line"] = "bottom_line"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Cellar Club"] = "cellar_club"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Chamber"] = "chamber"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Hall in Munich"] = "munich"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Hall in Vienna"] = "vienna"

            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["2ch Stereo"] = "all_ch_stereo"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["7ch Stereo"] = "all_ch_stereo"
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Straight"] = "straight"

            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms["Surround Decoder"] = "surr_decoder"
        }
    }

    fun deviceInfo(): DeviceInfo {
        val deviceInfo = URL("$yamahaReceiverUrl/YamahaExtendedControl/v1/system/getDeviceInfo").readText()
        return de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapper.readValue(deviceInfo, DeviceInfo::class.java)
    }

    fun features(): de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Features {
        val features = URL("$yamahaReceiverUrl/YamahaExtendedControl/v1/system/getFeatures").readText()
        return de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapper.readValue(features, de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.features.Features::class.java)
    }

    fun soundProgramList(): de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.SoundProgramList {
        val soundProgramList = URL("$yamahaReceiverUrl/YamahaExtendedControl/v1/main/getSoundProgramList").readText()
        return de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapper.readValue(soundProgramList, de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.SoundProgramList::class.java)
    }

    fun setVolume(volume: Int) {
        URL("$yamahaReceiverUrl/YamahaExtendedControl/v1/main/setVolume?volume=$volume").readText()
    }

    fun setSurroundProgram(program: String): de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.ResponseCode {
        println("Setting program '${de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms[program]}'")
        return try {
            de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapPrograms[program]
                ?.let { URL("$yamahaReceiverUrl/YamahaExtendedControl/v1/main/setSoundProgram?program=$it").readText() }
                ?.let { de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.client.YamahaAvantageReceiverClient.Companion.mapper.readValue(it, de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.ResponseCode::class.java) }
                ?: de.visualdigits.kotlin.klanglicht.model.yamahaadvantage.ResponseCode(0)

        } catch (e: JsonProcessingException) {
            throw IllegalStateException("Could not read from api", e)
        }
    }
}
