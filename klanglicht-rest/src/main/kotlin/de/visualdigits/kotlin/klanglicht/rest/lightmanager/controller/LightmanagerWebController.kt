package de.visualdigits.kotlin.klanglicht.rest.lightmanager.controller

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.feign.LightmanagerClient
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/v1/lightmanager/web")
class LightmanagerWebController {

    @Autowired
    var configHolder: ConfigHolder? = null

    @Autowired
    var client: LightmanagerClient? = null

    @Value("\${server.lightmanager.theme}")
    var theme: String? = null

    @GetMapping(value = ["/scenes"], produces = ["application/xhtml+xml"])
    fun scenes(
        @RequestParam(name = "lang", required = false, defaultValue = "de") lang: String?,
        model: Model, request: HttpServletRequest?
    ): String {
        model.addAttribute("theme", theme)
        model.addAttribute("title", "Scenes")
        val scenes = client?.scenes()
        model.addAttribute("content", scenes?.toHtml(configHolder!!))
        return "pagetemplate"
    }

    @GetMapping(value = ["/zones"], produces = ["application/xhtml+xml"])
    fun zones(
        @RequestParam(name = "lang", required = false, defaultValue = "de") lang: String?,
        model: Model, request: HttpServletRequest?
    ): String {
        model.addAttribute("theme", theme)
        model.addAttribute("title", "Zones")
        val zones = client?.zones()
        model.addAttribute("content", zones?.toHtml(configHolder!!))
        return "pagetemplate"
    }
}
