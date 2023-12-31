package de.visualdigits.kotlin.klanglicht.rest.lightmanager.controller

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.lightmanager.feign.LightmanagerClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/v1/lightmanager/web")
class LightmanagerWebController {

    @Autowired
    var configHolder: ConfigHolder? = null

    @Autowired
    var client: LightmanagerClient? = null

    @GetMapping("/scenes", produces = ["application/xhtml+xml"])
    fun scenes(model: Model): String {
        model.addAttribute("theme", configHolder?.preferences?.theme)
        model.addAttribute("title", "Scenes")
        val scenes = client?.scenes()
        model.addAttribute("content", scenes?.toHtml(configHolder!!))
        return "pagetemplate"
    }

    @GetMapping("/zones", produces = ["application/xhtml+xml"])
    fun zones(model: Model): String {
        model.addAttribute("theme", configHolder?.preferences?.theme)
        model.addAttribute("title", "Zones")
        val zones = client?.zones()
        model.addAttribute("content", zones?.toHtml(configHolder!!))
        return "pagetemplate"
    }
}
