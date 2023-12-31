package de.visualdigits.kotlin

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext

@SpringBootApplication
open class Application {

    companion object {
        private var context: ConfigurableApplicationContext? = null

        @JvmStatic
        fun main(args: Array<String>) {
            val application = SpringApplication(Application::class.java)
            context = application.run(*args)
        }

        fun restart(activeProfiles: String) {
            val args = context
                ?.getBean(ApplicationArguments::class.java)
                ?.sourceArgs
                ?:arrayOf()
            val thread = Thread {
                System.getProperties()["spring.profiles.active"] = activeProfiles
                context?.close()
                context = SpringApplication.run(Application::class.java, *args)
            }
            thread.isDaemon = false
            thread.start()
        }
    }
}
