package de.visualdigits.kotlin.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.visualdigits.kotlin.twinkly.model.device.AuthToken
import de.visualdigits.kotlin.twinkly.model.device.CONNECTION_TIMEOUT
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

open class UrlClient(
    val host: String
) {

    val log = LoggerFactory.getLogger(javaClass)

    val mapper = jacksonObjectMapper()

    companion object {
        val tokens: MutableMap<String, AuthToken> = mutableMapOf()
    }

    inline fun <reified T> post(
        url: String,
        body: ByteArray = byteArrayOf(),
        headers: MutableMap<String, String> = mutableMapOf(),
        authToken: String? = null
    ): T? {
        log.debug("POST '{}' body='{}' headers={}", url, String(body), headers)
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "POST"
        connection.connectTimeout = CONNECTION_TIMEOUT
        (authToken?: tokens[host]?.authToken)?.let { at -> headers["X-Auth-Token"] = at }
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        connection.doOutput = true
        return try {
            connection.outputStream.use { os ->
                os.write(body, 0, body.size)
            }
            val response = (if(connection.contentEncoding == "gzip") GZIPInputStream(connection.inputStream) else connection.inputStream).use { ins ->
                ins.readAllBytes()
            }
            when (T::class) {
                String::class -> String(response) as T
                else -> mapper.readValue(response, T::class.java)
            }
        } catch (e: Exception) {
            log.warn("Could not talk to server $host")
            null
        }
    }

    inline fun <reified T> get(
        url: String,
        headers: Map<String, String> = mapOf()
    ): T? {
        log.debug("GET '{}' headers={}", url, headers)
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "GET"
        connection.connectTimeout = CONNECTION_TIMEOUT
        tokens[host]?.authToken?.let { at -> connection.setRequestProperty("X-Auth-Token", at) }
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        return try {
            val response = (if(connection.contentEncoding == "gzip") GZIPInputStream(connection.inputStream) else connection.inputStream).use { ins ->
                ins.readAllBytes()
            }
            when (T::class) {
                String::class -> String(response) as T
                else -> mapper.readValue(response, T::class.java)
            }
        } catch (e: Exception) {
            log.warn("Could not talk to server $host")
            null
        }
    }

    inline fun <reified T> delete(
        url: String,
        headers: Map<String, String> = mapOf()
    ): T? {
        log.debug("DELETE '{}' headers={}", url, headers)
        val connection = (URL(url).openConnection() as HttpURLConnection)
        connection.requestMethod = "DELETE"
        connection.connectTimeout = CONNECTION_TIMEOUT
        tokens[host]?.authToken?.let { at -> connection.setRequestProperty("X-Auth-Token", at) }
        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
        return try {
            val response = (if(connection.contentEncoding == "gzip") GZIPInputStream(connection.inputStream) else connection.inputStream).use { ins ->
                ins.readAllBytes()
            }
            when (T::class) {
                String::class -> String(response) as T
                else -> mapper.readValue(response, T::class.java)
            }
        } catch (e: Exception) {
            log.warn("Could not talk to server $host")
            null
        }
    }
}
