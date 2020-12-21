package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.runtime.StringBuilderOutput
import io.micronaut.context.env.Environment
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import technology.iatlas.spaceup.config.DevUrlConfig
import technology.iatlas.spaceup.core.annotations.ClientLink
import technology.iatlas.spaceup.services.ClassService


@Controller("/")
class HomeController(config: DevUrlConfig, env: Environment, classService: ClassService) :
    BaseController(config, env, classService) {
    // private val log = LoggerFactory.getLogger(HomeController::class.java)

    @ClientLink("Home")
    @Get(produces = [MediaType.TEXT_HTML])
    fun home(): String {
        return getRockerTemplate("views/dashboard.rocker.html")
            .render(StringBuilderOutput.FACTORY)
            .toString()
    }

}