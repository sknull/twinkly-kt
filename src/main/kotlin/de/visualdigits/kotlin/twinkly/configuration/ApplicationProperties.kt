package de.visualdigits.kotlin.twinkly.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "application")
class ApplicationProperties {

    var devices: Map<String, String> = mapOf()
}