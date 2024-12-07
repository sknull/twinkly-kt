package de.visualdigits.kotlin.klanglicht.rest.configuration

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.filter.AbstractRequestLoggingFilter

class SimpleRequestLoggingFilter : AbstractRequestLoggingFilter() {

    @Value("\${server.logging.requestLogging}")
    private var enableReuestLogging: Boolean? = false

    override fun beforeRequest(request: HttpServletRequest, message: String) {
        if (enableReuestLogging == true) {
            logger.info(message)
        }
    }

    override fun afterRequest(request: HttpServletRequest, message: String) {
        // intentionally empty to prevent duplicate after logging
    }
}
