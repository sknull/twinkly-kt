package de.visualdigits.kotlin.klanglicht.rest.yamaha.controller

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.yamaha.feign.YamahaReceiverClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/v1/yamaha/xml")
class YamahaReceiverRestController {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    val configHolder: ConfigHolder? = null

    var client: YamahaReceiverClient? = null

    @PutMapping("/surroundProgram")
    fun controlSurroundProgram(@RequestParam("program") program: String) {
        ensureClient()
        log.info("Setting surround sound program to '$program'")
        client?.controlSurroundProgram(program)
    }

    private fun ensureClient() {
        if (client == null) {
            client = configHolder!!.preferences?.serviceMap?.get("receiver")?.url?.let { YamahaReceiverClient(it) }
        }
    }
}
