package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.runtime.StringBuilderOutput
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import technology.iatlas.spaceup.core.annotations.WebNavigation
import technology.iatlas.spaceup.dto.Disk
import technology.iatlas.spaceup.services.ServerService


@Controller("/")
class HomeController(private val serverService: ServerService) : BaseController() {
    // private val log = LoggerFactory.getLogger(HomeController::class.java)

    @WebNavigation("Home", mapping = "/", prio = 0)
    @Get(produces = [MediaType.TEXT_HTML])
    suspend fun home(): String {
        val hostname = serverService.getHostname()

        // if quota is null, there was probably something wrong with parsing
        val diskQuota = serverService.getDiskQuota()

        return getRockerTemplate("views/dashboard.rocker.html")
            .bind("hostname", hostname)
            .bind("diskQuota", diskQuota)
            .render(StringBuilderOutput.FACTORY)
            .toString()
    }

}