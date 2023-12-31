package de.visualdigits.kotlin.klanglicht.rest.shelly.controller

import de.visualdigits.kotlin.klanglicht.rest.configuration.ConfigHolder
import de.visualdigits.kotlin.klanglicht.rest.shelly.handler.ShellyHandler
import de.visualdigits.kotlin.klanglicht.rest.shelly.model.html.ShellyStatus
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/v1/shelly/web")
class ShellyWebController {

    @Autowired
    var configHolder: ConfigHolder? = null

    @Autowired
    var shellyHandler: ShellyHandler? = null

    @GetMapping("powers", produces = ["application/xhtml+xml"])
    fun currentPowers(model: Model, request: HttpServletRequest?): String {
        model.addAttribute("theme", configHolder?.preferences?.theme)
        model.addAttribute("title", "Current Power Values")
        model.addAttribute("content", ShellyStatus().toHtml(shellyHandler!!))
        return "pagetemplate"
    }
}
