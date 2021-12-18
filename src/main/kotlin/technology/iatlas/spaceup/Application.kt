package technology.iatlas.spaceup

import io.micronaut.runtime.Micronaut.build
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
            title = "SpaceUp",
            version = "0.12"
    )
)
object Api {}

fun main(args: Array<String>) {
	build()
        .eagerInitSingletons(true)
	    .args(*args)
		.packages("technology.iatlas.spaceup")
        .defaultEnvironments("prod")
		.start()
}

