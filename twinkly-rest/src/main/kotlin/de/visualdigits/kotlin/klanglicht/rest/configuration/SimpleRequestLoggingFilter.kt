package de.visualdigits.kotlin.klanglicht.rest.configuration

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.filter.AbstractRequestLoggingFilter

open class SimpleRequestLoggingFilter : AbstractRequestLoggingFilter() {

    override fun beforeRequest(request: HttpServletRequest, message: String) {
        logger.info(message)
    }

    override fun afterRequest(request: HttpServletRequest, message: String) {
        // intentionally empty to prevent duplicate after logging
    }
}
