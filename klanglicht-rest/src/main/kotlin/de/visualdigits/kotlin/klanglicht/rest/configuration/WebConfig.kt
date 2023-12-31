package de.visualdigits.kotlin.klanglicht.rest.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import org.springframework.web.servlet.ViewResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.view.ThymeleafViewResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.FileTemplateResolver
import org.thymeleaf.templateresolver.ITemplateResolver
import java.nio.file.Paths

@Configuration
open class WebConfig : WebMvcConfigurer {

    @Autowired
    val configHolder: ConfigHolder? = null

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }

    @Bean
    @Description("Thymeleaf file system template resolver serving HTML 5")
    open fun templateResolver(): ITemplateResolver {
        val templateResolver = FileTemplateResolver()
        val templatesPath = Paths.get(
            configHolder!!.klanglichtDirectory.absolutePath,
            "resources",
            "themes",
            configHolder.preferences?.theme,
            "templates"
        ).toFile().absolutePath.replace("\\", "/") + "/"
        templateResolver.prefix = templatesPath
        templateResolver.isCacheable = false
        templateResolver.suffix = ".html"
        templateResolver.templateMode = TemplateMode.HTML
        templateResolver.characterEncoding  = "UTF-8"
        return templateResolver
    }

    @Bean
    @Description("Thymeleaf template engine with Spring integration")
    open fun templateEngine(): SpringTemplateEngine {
        val templateEngine = SpringTemplateEngine()
        templateEngine.setTemplateResolver(templateResolver())
        return templateEngine
    }

    @Bean
    @Description("Thymeleaf view resolver")
    open fun viewResolver(): ViewResolver {
        val viewResolver = ThymeleafViewResolver()
        viewResolver.templateEngine = templateEngine()
        viewResolver.characterEncoding = "UTF-8"
        return viewResolver
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/").setViewName("index")
    }
}
