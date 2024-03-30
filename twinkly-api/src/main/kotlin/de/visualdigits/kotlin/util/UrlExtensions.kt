package de.visualdigits.kotlin.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.visualdigits.kotlin.twinkly.model.device.CONNECTION_TIMEOUT
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream


val mapper = jacksonObjectMapper()

inline fun <reified T> URL.post(
    body: ByteArray = byteArrayOf(),
    headers: MutableMap<String, String> = mutableMapOf()
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
        null
    }
}
