package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.Rocker
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import technology.iatlas.spaceup.core.annotations.ClientLink
import technology.iatlas.spaceup.services.RouterService


@Controller("/")
@ClientLink("Dashboard")
class HomeController(routerService: RouterService) : BaseController(routerService) {
    // private val log = LoggerFactory.getLogger(HomeController::class.java)

    @Get(produces = [MediaType.TEXT_HTML])
    fun home(): String {
        return Rocker.template("views/dashboard.rocker.html")
            .bind("mapLinks", mapLinks)
            .render()
            .toString()
    }
}