package technology.iatlas.spaceup.controller.web

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.WebNavigation
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.Service
import technology.iatlas.spaceup.dto.ServiceOption
import technology.iatlas.spaceup.services.ServiceService

@Controller("/services")
open class ServiceController(private val serviceService: ServiceService) : BaseController() {
    private val log = LoggerFactory.getLogger(ServiceController::class.java)

    @WebNavigation("Services", "/services", prio = 1)
    @Get(produces = [MediaType.TEXT_HTML])
    @Cacheable("cache-service-list")
    open fun list(): String {

        val services = serviceService.list()
        val options = serviceService.options()

        return getRockerTemplate("views/services.rocker.html")
            .bind("services", services)
            .bind("options", options)
            .render()
            .toString()
    }

}