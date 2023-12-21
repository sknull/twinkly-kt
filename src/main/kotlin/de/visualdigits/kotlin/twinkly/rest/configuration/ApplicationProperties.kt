package de.visualdigits.kotlin.twinkly.rest.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "server")
class ApplicationProperties {

    var array: XledArrayConfiguration? = null
}
