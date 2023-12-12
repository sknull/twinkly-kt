package de.visualdigits.kotlin.twinkly.model.device.xled.response

enum class ResponseCode(val code: Int) {

    Ok(1000),
    Error1(1001),
    InvalidArgumentValue(1101),
    Error2(1102),
    ValueTooLong(1103),
    MalformedJson(1104),
    InvalidArgumentKey(1105),
    Ok2(1107),
    Ok3(1108),
    UpdateErrorSHA(1205),

    Unknown(0)
    ;

    companion object {
        fun byCode(code: Int): ResponseCode {
            return ResponseCode.entries.find { code == it.code }?: Unknown
        }
    }
}
