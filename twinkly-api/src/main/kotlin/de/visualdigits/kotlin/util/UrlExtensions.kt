package de.visualdigits.kotlin.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import de.visualdigits.kotlin.twinkly.model.device.xled.XLedDevice
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

@Suppress("UNCHECKED_CAST")
fun <T : Any> URL.post(
    body: ByteArray = byteArrayOf(),
    headers: Map<String, String> = mapOf(),
    clazz: Class<T>
): T? {
    val connection = (openConnection() as HttpURLConnection)
    connection.requestMethod = "POST"
    connection.connectTimeout = XLedDevice.CONNECTION_TIMEOUT
    connection.readTimeout = XLedDevice.CONNECTION_TIMEOUT
    headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
    connection.doOutput = true
    connection.outputStream.use { os ->
        os.write(body, 0, body.size)
    }
    val response = (if(connection.contentEncoding == "gzip") GZIPInputStream(connection.inputStream) else connection.inputStream).use { ins ->
        ins.readAllBytes()
    }
    return when (clazz) {
        String::class.java -> String(response) as T
        else -> mapper.readValue(response, clazz)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> URL.put(
    body: ByteArray = byteArrayOf(),
    headers: Map<String, String> = mapOf(),
    clazz: Class<T>
): T? {
    val connection = createConnection("PUT", headers, true)
    connection.outputStream.use { os ->
        os.write(body, 0, body.size)
    }
    val response = (if(connection.contentEncoding == "gzip") GZIPInputStream(connection.inputStream) else connection.inputStream).use { ins ->
        ins.readAllBytes()
    }
    val res = String(response)
    return when (clazz) {
        String::class.java -> res as T
        else -> mapper.readValue(res, clazz)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> URL.get(
    headers: Map<String, String> = mapOf(),
    clazz: Class<T>
): T? {
    val connection = createConnection("GET", headers)
    val response =
        (if (connection.contentEncoding == "gzip") GZIPInputStream(connection.inputStream) else connection.inputStream)
            .use { ins ->
                ins.readAllBytes()
            }
    val res = String(response)
    return when (clazz) {
        String::class.java -> res as T
        else -> mapper.readValue<T>(res, clazz)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> URL.delete(
    headers: Map<String, String> = mapOf(),
    clazz: Class<T>
): T? {
    val connection = createConnection("DELETE", headers)
    val response = (if(connection.contentEncoding == "gzip") GZIPInputStream(connection.inputStream) else connection.inputStream).use { ins ->
        ins.readAllBytes()
    }
    val res = String(response)
    return when (clazz) {
        String::class.java -> res as T
        else -> mapper.readValue(res, clazz)
    }
}

private fun URL.createConnection(
    method: String,
    headers: Map<String, String>,
    doOutput: Boolean = false
): HttpURLConnection {
    val connection = (openConnection() as HttpURLConnection)
    connection.requestMethod = method
    connection.connectTimeout = XLedDevice.CONNECTION_TIMEOUT
    headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }
    if (doOutput) {
        connection.doOutput = true
    }
    return connection
}
