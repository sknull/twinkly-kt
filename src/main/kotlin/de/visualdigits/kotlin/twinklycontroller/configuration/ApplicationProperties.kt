package de.visualdigits.kotlin.twinklycontroller.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "server")
class ApplicationProperties {

    var array: XledArrayConfiguration? = null
}
