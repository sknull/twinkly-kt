package de.visualdigits.kotlin.util

import de.visualdigits.kotlin.twinkly.model.device.AuthToken
import org.slf4j.LoggerFactory
import java.net.URL

const val HEADER_X_AUTH_TOKEN = "X-Auth-Token"

open class UrlClient(
    val host: String
) {

    val log = LoggerFactory.getLogger(javaClass)

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
        (authToken?: tokens[host]?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return try {
            URL(null, url).post<T>(body, headers)
        } catch (e: Exception) {
            log.warn("Could not talk to server $host")
            null
        }
    }

    inline fun <reified T> get(
        url: String,
        headers: MutableMap<String, String> = mutableMapOf(),
        authToken: String? = null
    ): T? {
        log.debug("GET '{}' headers={}", url, headers)
        (authToken?: tokens[host]?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return try {
            URL(null, url).get<T>(headers)
        } catch (e: Exception) {
            log.warn("Could not talk to server $host")
            null
        }
    }

    inline fun <reified T> delete(
        url: String,
        headers: MutableMap<String, String> = mutableMapOf(),
        authToken: String? = null
    ): T? {
        log.debug("DELETE '{}' headers={}", url, headers)
        (authToken?: tokens[host]?.authToken)?.let { at -> headers[HEADER_X_AUTH_TOKEN] = at }
        return try {
            URL(null, url).delete<T>(headers)
        } catch (e: Exception) {
            log.warn("Could not talk to server $host")
            null
        }
    }
}
