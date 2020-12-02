package technology.iatlas.spaceup.controller

import com.google.gson.Gson
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured

@Controller("/auth")
class AuthenticationController {

    @Get(produces = [MediaType.APPLICATION_JSON])
    @Secured("isAnonymous()")
    fun index(): String {
        val gson = Gson()
        return gson.toJson(mapOf(0 to "Hello", 1 to "World"))
    }
}