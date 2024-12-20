package de.visualdigits.kotlin.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import de.visualdigits.kotlin.twinkly.model.device.CONNECTION_TIMEOUT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream

object UrlExtensions {
    val log: Logger = LoggerFactory.getLogger(javaClass)
}

val mapper: JsonMapper = jacksonMapperBuilder()
    .disable(SerializationFeature.INDENT_OUTPUT)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .addModule(JavaTimeModule())
    .build()

inline fun <reified T> URL.post(
    body: ByteArray = byteArrayOf(),
    headers: Map<String, String> = mapOf()
): T? {
    val connection = (openConnection() as HttpURLConnection)
    connection.requestMethod = "POST"
    connection.connectTimeout = CONNECTION_TIMEOUT
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
        UrlExtensions.log.warn("Could not post to '${this}': {}", e.message)
        null
    }
}

inline fun <reified T> URL.get(
    headers: Map<String, String> = mapOf()
): T? {
    val connection = (openConnection() as HttpURLConnection)
    connection.requestMethod = "GET"
    connection.connectTimeout = CONNECTION_TIMEOUT
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
        UrlExtensions.log.warn("Could not get from '${this}': {}", e.message)
        null
    }
}

inline fun <reified T> URL.delete(
    headers: Map<String, String> = mapOf()
): T? {
    val connection = (openConnection() as HttpURLConnection)
    connection.requestMethod = "DELETE"
    connection.connectTimeout = CONNECTION_TIMEOUT
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
        UrlExtensions.log.warn("Could not delete from '${this}': {}", e.message)
        null
    }
}
