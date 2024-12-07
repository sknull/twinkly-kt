package de.visualdigits.kotlin.klanglicht.rest.configuration

import de.visualdigits.kotlin.klanglicht.model.preferences.Preferences
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration
import java.io.File
import java.nio.file.Paths

@Configuration
@ConfigurationProperties(prefix = "application")
@ConfigurationPropertiesScan
open class ApplicationPreferences {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    var preferences: Preferences? = null

    @Value("\${application.klanglichtDirectory}")
    var klanglichtDirectory: File = File("/")

    @PostConstruct
    fun initialize() {
        preferences?.initialize(klanglichtDirectory)

        log.info("#### setUp - start")
        log.info("##")
        log.info("## klanglichtDirectory: " + klanglichtDirectory.absolutePath)
        log.info("#### setUp - end")
    }

    @PreDestroy
    fun tearDown() {
        log.info("#### tearDown - start")
        log.info("#### tearDown - end")
    }

    fun getAbsoluteResource(relativeResourePath: String): File {
        return Paths.get(klanglichtDirectory.absolutePath, "resources", relativeResourePath).toFile()
    }
}
