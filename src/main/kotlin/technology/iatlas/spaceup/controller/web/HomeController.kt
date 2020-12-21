package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.runtime.StringBuilderOutput
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import technology.iatlas.spaceup.core.annotations.WebNavigation


@Controller("/")
class HomeController : BaseController() {
    // private val log = LoggerFactory.getLogger(HomeController::class.java)

    @WebNavigation("Home", mapping = "/", prio = 0)
    @Get(produces = [MediaType.TEXT_HTML])
    fun home(): String {
        return getRockerTemplate("views/dashboard.rocker.html")
            .render(StringBuilderOutput.FACTORY)
            .toString()
    }

}