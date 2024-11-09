package edu.ingsis.permission

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class PermissionManagerApplication {
    @Bean
    fun docs(): OpenAPI {
        return OpenAPI()
            .info(
                Info().title("Permission Manager API").version("v1"),
            ).servers(emptyList())
    }
}

fun main(args: Array<String>) {
    runApplication<PermissionManagerApplication>(*args)
}
