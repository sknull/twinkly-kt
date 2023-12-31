package de.visualdigits.kotlin.klanglicht.rest

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.tika.detect.Detector
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.apache.tika.mime.MediaType
import org.apache.tika.parser.AutoDetectParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Controller
class ResourceController {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    val configHolder: ConfigHolder? = null

    @GetMapping("/resources/**")
    @ResponseBody
    fun resource(request: HttpServletRequest, response: HttpServletResponse) {
        val src = getRequestUri(request)?.substring("/resources".length)?:""
        val file = configHolder!!.getAbsoluteResource(src)
        try {
            FileInputStream(file).use { ins ->
                response.outputStream.use { outs ->
                    val mimeType = detectMimeType(file)
                    response.contentType = mimeType
                    ins.copyTo(outs)
                }
            }
        } catch (e: IOException) {
            log.warn("Could not hand out resource: $src")
        }
    }

    private fun detectMimeType(file: File): String {
        var mimeType = "text/plain"
        try {
            FileInputStream(file).use { `is` ->
                BufferedInputStream(`is`).use { bis ->
                    val parser = AutoDetectParser()
                    val detector: Detector = parser.detector
                    val md = Metadata()
                    md.add(TikaCoreProperties.RESOURCE_NAME_KEY, file.name)
                    val mediaType: MediaType = detector.detect(bis, md)
                    mimeType = mediaType.toString()
                }
            }
        } catch (e: IOException) {
            log.debug("Could not detrmine mime type for resource '$file'")
        }
        return mimeType
    }

    protected fun getRequestUri(request: HttpServletRequest): String? {
        return try {
            URLDecoder.decode(request.requestURI, request.characterEncoding)
        } catch (e: UnsupportedEncodingException) {
            log.debug("Could not decode url '$request'")
            null
        }
    }

    protected fun encodeUrl(response: HttpServletResponse, pagePath: String?): String? {
        var pagePath = pagePath
        val url = pagePath
        try {
            pagePath = URLEncoder.encode(pagePath, response.characterEncoding)
        } catch (e: UnsupportedEncodingException) {
            // ignore
        }
        return pagePath
    }

    protected fun readFile(xslFile: File): String? {
        var content: String? = null
        try {
            FileInputStream(xslFile).use { ins ->
                ByteArrayOutputStream().use { baos ->
                    ins.copyTo(baos)
                    content = baos.toString()
                }
            }
        } catch (e: IOException) {
            log.error("Could not read xsl file: $xslFile", e)
        }
        return content
    }

    protected fun sendContent(content: String, mimeType: String?, response: HttpServletResponse) {
        sendContent(content, mimeType, null, response)
    }

    protected fun sendContent(
        content: String,
        mimeType: String?,
        headers: Map<String?, String?>?,
        response: HttpServletResponse
    ) {
        try {
            ByteArrayInputStream(content.toByteArray(StandardCharsets.UTF_8)).use { ins ->
                response.outputStream.use { outs ->
                    response.contentType = mimeType
                    headers?.forEach(response::addHeader)
                    ins.copyTo(outs)
                }
            }
        } catch (e: IOException) {
            throw IllegalStateException("Could not hand out rss stream", e)
        }
    }

    companion object {
        protected const val MIMETYPE_XML = "application/xml"
        protected const val MIMETYPE_XSL = "text/xsl"
        const val HEADER_ACCEPT_RSS =
            "application/rss+xml, application/rdf+xml;q=0.8, application/atom+xml;q=0.6, application/xml;q=0.4, text/xml;q=0.4"
    }
}
