package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.Rocker
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured


@Controller("/")
class HomeController {

    @Get(produces = [MediaType.TEXT_HTML])
    @Secured("isAnonymous()")
    fun home(): String {

        return Rocker.template("views/dashboard.rocker.html")
            .bind("mapLinks", mapOf("Home" to "/"))
            .render()
            .toString()
    }

}