package technology.iatlas.spaceup.controller

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured

@Controller("/hello")
class HelloController {

    @Get(produces = [MediaType.TEXT_PLAIN])
    @Secured("isAnonymous()")
    fun index(): String {
        return "Hello World"
    }
}