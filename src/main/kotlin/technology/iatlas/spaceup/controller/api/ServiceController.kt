package technology.iatlas.spaceup.controller.api

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.Service
import technology.iatlas.spaceup.dto.ServiceOption
import technology.iatlas.spaceup.services.ServiceService

@Controller("/api/service")
class ServiceController(private val serviceService: ServiceService) {
    private val log = LoggerFactory.getLogger(ServiceController::class.java)

    @Post("/execute/{name}/{option}")
    fun execute(name: String, option: String): Feedback? {
        log.debug("Execute $name: $option")

        val serviceName = Service(name, null, null)
        val serviceOption = ServiceOption.valueOf(option)

        return serviceService.execute(serviceName, serviceOption)
    }
}