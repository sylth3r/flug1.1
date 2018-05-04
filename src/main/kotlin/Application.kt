package de.hska.flug

import de.hska.kunde.config.Settings.BANNER
import de.hska.kunde.config.Settings.PROPS
import de.hska.flug.entity.Flug
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import de.hska.flug.rest.FlugHandler
import org.springframework.boot.WebApplicationType.REACTIVE

@SpringBootApplication
class Application {
    @Bean
    fun router(handler: FlugHandler) = org.springframework.web.reactive.function.server.router {
        "/".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                GET("/", handler::find)
                GET("/$ID_PATH_PATTERN", handler::findById)
                POST("/", handler::create)
                DELETE("/$ID_PATH_PATTERN", handler::deleteById)
            }
        }
}
    companion object {
        /**
         * Name der Pfadvariablen für IDs.
         */
        val ID_PATH_VAR = "id"

        private
        val ID_PATH_PATTERN = "{$ID_PATH_VAR:${Flug.ID_PATTERN}}"
    }
}
fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    (org.springframework.boot.runApplication<Application>(*args) {
        webApplicationType = REACTIVE
        setBanner(BANNER)
        setDefaultProperties(PROPS)
        addListeners(ApplicationPidFileWriter())
    })
}
