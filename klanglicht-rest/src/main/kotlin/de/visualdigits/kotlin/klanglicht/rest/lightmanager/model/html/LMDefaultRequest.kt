package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html

import com.fasterxml.jackson.annotation.JsonIgnore


class LMDefaultRequest(
    var name: String? = null,
    var type: RequestType? = null,
    var deviceId: Long = 0,
    var actorId: Int? = null,
    var actorCommand: Int? = null,
    var sequence: Int? = null,
    var level: Int? = null,
    var smk: IntArray? = null,
    var uri: String? = null,
    var data: Array<String> = arrayOf()
) : LMRequest {

    @JsonIgnore
    fun requestTemplate(): String {
        val params = mutableListOf(
                "typ", type?.name,
                "did", deviceId.toString(),
                "aid", actorId.toString(),
                "acmd", actorCommand.toString(),
                "lvl", "\${level}",
                "seq", sequence.toString()
        )
        if (hasSmk()) {
            params.addAll(listOf("smk", smk!![0].toString(), smk!![1].toString()))
        }
        return "cmd=\"${params.joinToString(",")}\""
    }

    fun hasSmk(): Boolean {
        return smk != null && smk?.isNotEmpty() == true
    }
}
