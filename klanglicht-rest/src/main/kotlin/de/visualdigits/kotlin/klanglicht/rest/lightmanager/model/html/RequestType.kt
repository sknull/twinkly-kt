package de.visualdigits.kotlin.klanglicht.rest.lightmanager.model.html


enum class RequestType(
    val type: String
) {
    
    INTERTECHNO("it"),
    RF_433("rf4"),
    RF_868("rf8"),
    HOMEMATIC("rfh"),
    ELDAT("rfe"),
    PC("pc"),
    UNIROLL("uni"),
    SONOS("son"),
    GIRA("in"),
    ROMOTEC("rom"),
    SOMFY("som"),
    ALEXA_BELL("db"),
    GET("get"),
    POST("post"),
    PUT("put"),
    UDP("udp"),
    TCP("tcp"),
    WAKE_ON_LAN("wol"),
    UNKNOWN("?");

    companion object {
        fun getByName(type: String): RequestType? {
            var requestType: RequestType? = null
            for (t in values()) {
                if (t.type == type) {
                    requestType = t
                    break
                }
            }
            return requestType
        }
    }
}
