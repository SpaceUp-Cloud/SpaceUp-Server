package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.Rocker
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import org.slf4j.LoggerFactory


@Controller("/")
class HomeController {

    @Get(produces = [MediaType.TEXT_HTML])
    @Secured("isAnonymous()")
    fun home(): String {

        // Funktioniert irgendwie nicht
        /*return base.template("World")
            .render()
            .toString();*/

        return Rocker.template("views/home.rocker.html")
            .bind("message", "Gino")
            .render()
            .toString()
    }

}