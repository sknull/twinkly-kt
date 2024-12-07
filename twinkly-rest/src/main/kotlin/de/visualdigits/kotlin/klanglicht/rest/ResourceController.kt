package de.visualdigits.kotlin.klanglicht.rest

import de.visualdigits.kotlin.klanglicht.rest.configuration.ApplicationPreferences
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.tika.detect.Detector
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.apache.tika.mime.MediaType
import org.apache.tika.parser.AutoDetectParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URLDecoder

@Controller
class ResourceController(
    val prefs: ApplicationPreferences
) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("/resources/**")
    fun resource(request: HttpServletRequest, response: HttpServletResponse) {
        val src = URLDecoder
            .decode(request.requestURI, request.characterEncoding)
            ?.substring("/resources".length)?:""
        val file = prefs.getAbsoluteResource(src)
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
            log.debug("Could not detrmine mime type for resource '{}'", file)
        }
        return mimeType
    }
}
