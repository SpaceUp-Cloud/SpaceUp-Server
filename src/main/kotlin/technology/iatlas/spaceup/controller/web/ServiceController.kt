package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.runtime.StringBuilderOutput
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.WebNavigation
import technology.iatlas.spaceup.services.ServiceService

@Controller("/services")
class ServiceController(private val serviceService: ServiceService) : BaseController() {
    private val log = LoggerFactory.getLogger(ServiceController::class.java)

    @WebNavigation("Services", "/services", prio = 1)
    @Get(produces = [MediaType.TEXT_HTML])
    suspend fun list(): String {

        val services = serviceService.list()
        val options = serviceService.options()

        return getRockerTemplate("views/services.rocker.html")
            .bind("services", services)
            .bind("options", options)
            .render(StringBuilderOutput.FACTORY)
            .toString()
    }

}