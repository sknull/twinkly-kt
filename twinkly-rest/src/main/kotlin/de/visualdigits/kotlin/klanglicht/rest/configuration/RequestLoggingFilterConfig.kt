package de.visualdigits.kotlin.klanglicht.rest.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RequestLoggingFilterConfig {

    @Bean
    open fun logFilter(): SimpleRequestLoggingFilter {
        val filter = SimpleRequestLoggingFilter()
        filter.setBeforeMessagePrefix("Request [")
        filter.isIncludeQueryString = true
        filter.isIncludeClientInfo = true
        filter.isIncludeHeaders = true
//        filter.setIncludePayload(true);
//        filter.setMaxPayloadLength(10000);
        return filter
    }
}
