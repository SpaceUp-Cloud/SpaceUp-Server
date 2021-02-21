package technology.iatlas.spaceup.controller.api

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.Service
import technology.iatlas.spaceup.dto.ServiceOption
import technology.iatlas.spaceup.services.ServiceService

@Controller("/api/service")
class ServiceController(private val serviceService: ServiceService) {
    private val log = LoggerFactory.getLogger(ServiceController::class.java)

    /**
     * Execute an operation change to a Service
     */
    @Post("/execute/{name}/{option}")
    suspend fun execute(name: String, option: ServiceOption): Feedback {
        log.debug("Execute $name: $option")

        val serviceName = Service(name, null, null)
        return serviceService.execute(serviceName, option)
    }

    /**
     * Get all services with their statuses
     */
    @Get("/list", produces = [MediaType.APPLICATION_JSON])
    suspend fun list(): List<Service> {
        return serviceService.list()
    }
}