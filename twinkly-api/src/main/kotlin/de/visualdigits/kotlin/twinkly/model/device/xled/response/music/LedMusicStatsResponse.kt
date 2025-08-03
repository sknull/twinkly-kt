package de.visualdigits.kotlin.twinkly.model.device.xled.response.music


import com.fasterxml.jackson.annotation.JsonProperty
import de.visualdigits.kotlin.twinkly.model.common.JsonObject

class LedMusicStatsResponse(
    code: Int? = null,

    @JsonProperty("total_skipped") val totalSkipped: Int? = null,
    @JsonProperty("total_received") val totalReceived: Int? = null,
    @JsonProperty("total_loss_percent") val totalLossPercent: Int? = null,

    @JsonProperty("last_rcvd_packets") val lastRcvdPackets: Int? = null,
    @JsonProperty("last_skipped") val lastSkipped: Int? = null,
    @JsonProperty("last_skipped_percent") val lastSkippedPercent: Int? = null,
    @JsonProperty("last_packet_rate") val lastPacketRate: Int? = null,
    @JsonProperty("last_elapsed") val lastElapsed: Int? = null,

    @JsonProperty("diff_avg") val diffAvg: Int? = null,
    @JsonProperty("diff_stdev") val diffStdev: Int? = null,
    @JsonProperty("diff_min") val diffMin: Int? = null,
    @JsonProperty("diff_max") val diffMax: Int? = null,

    @JsonProperty("local_delta_avg") val localDeltaAvg: Int? = null,
    @JsonProperty("local_delta_stdev") val localDeltaStdev: Int? = null,
    @JsonProperty("local_delta_min") val localDeltaMin: Int? = null,
    @JsonProperty("local_delta_max") val localDeltaMax: Int? = null,

    @JsonProperty("remote_delta_avg") val remoteDeltaAvg: Int? = null,
    @JsonProperty("remote_delta_stdev") val remoteDeltaStdev: Int? = null,
    @JsonProperty("remote_delta_min") val remoteDeltaMin: Int? = null,
    @JsonProperty("remote_delta_max") val remoteDeltaMax: Int? = null,

    @JsonProperty("new_beats") val newBeats: Int? = null,
    val bpm: Int? = null,
) : JsonObject(code)
