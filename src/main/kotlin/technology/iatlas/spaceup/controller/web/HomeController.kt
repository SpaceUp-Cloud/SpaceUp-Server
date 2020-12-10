package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.Rocker
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.ClientLink
import technology.iatlas.spaceup.services.RouterService


@Controller("/")
@ClientLink("Dashboard")
class HomeController(private val routerService: RouterService) {
    // private val log = LoggerFactory.getLogger(HomeController::class.java)

    @Get(produces = [MediaType.TEXT_HTML])
    @Secured("isAnonymous()")
    fun home(): String {
        val mapLinks = routerService.getClientControllerLinking()

        return Rocker.template("views/dashboard.rocker.html")
            .bind("mapLinks", mapLinks)
            .render()
            .toString()
    }

}