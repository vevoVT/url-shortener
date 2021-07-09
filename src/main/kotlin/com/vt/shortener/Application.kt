package com.vt.shortener

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "url_shortener",
        version = "v1",
        description = "API for creating & viewing short urls along with redirecting to the original url"
    )
)
object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .args(*args)
            .packages("com.vt.shortener")
            .start()
    }
}
