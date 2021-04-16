package technology.iatlas.spaceup.controller.api

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.SpaceupPathConfig
import technology.iatlas.spaceup.dto.*
import technology.iatlas.spaceup.services.ServiceService
import javax.annotation.Nullable

@Controller("/api/service")
class ServiceController(private val serviceService: ServiceService,
                        private val config: SpaceupPathConfig) {
    private val log = LoggerFactory.getLogger(ServiceController::class.java)

    /**
     * Execute an operation change to a Service
     */
    @Post("/execute/{servicename}/{option}")
    suspend fun execute(servicename: String, option: ServiceOption): Feedback {
        log.debug("Execute $servicename: $option")

        val serviceName = Service(servicename, null, null)
        return serviceService.execute(serviceName, option)
    }

    /**
     * Get all services with their statuses
     */
    @Get("/list", produces = [MediaType.APPLICATION_JSON])
    suspend fun list(): List<Service> {
        return serviceService.list()
    }

    @Delete("/delete/{servicename}")
    suspend fun deleteService(servicename: String): Feedback {
        log.warn("Delete service: $servicename")

        return serviceService.deleteService(servicename, config.services)
    }

    /**
     * Retrieve Stdout and Stderr logs
     */
    @Get(uri = "/logs/{servicename}/{?limits}", produces = [MediaType.APPLICATION_JSON])
    fun getLogs(servicename: String, @Nullable limits: Int = 500): HttpResponse<Map<Logtype, Logfile>> {
        return HttpResponse.ok(serviceService.getLogs(servicename, limits))
    }

    /**
     * Retrieve one kind of log
     */
    @Get(uri = "/logs/{servicename}/{type}/{?limits}", produces = [MediaType.APPLICATION_JSON])
    fun getLogs(servicename: String, type: Logtype, @Nullable limits: Int = 500): HttpResponse<Logfile> {
        return HttpResponse.ok(serviceService.getLogs(servicename, type, limits))
    }
}