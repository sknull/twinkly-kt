package de.visualdigits.kotlin.klanglicht.rest.lightmanager.feign

import feign.Feign
import feign.Logger
import feign.Param
import feign.RequestLine
import feign.okhttp.OkHttpClient
import feign.slf4j.Slf4jLogger

interface LightmanagerFeignClient {

    @RequestLine("GET /")
    fun html(): String?

    @RequestLine("GET /config.xml")
    fun configXml(): String?

    @RequestLine("GET /params.json")
    fun paramsJson(): String?

    @RequestLine("POST /control?key={scene}")
    fun controlScene(@Param("scene") sceneId: Int)

    @RequestLine("POST /control?scene={index}")
    fun controlIndex(@Param("index") index: Int)

    companion object {
        fun client(url: String?): LightmanagerFeignClient {
            return Feign.builder()
                .client(OkHttpClient())
                .logger(Slf4jLogger(LightmanagerFeignClient::class.java))
                .logLevel(Logger.Level.BASIC)
                .target(LightmanagerFeignClient::class.java, url)
        }
    }
}
