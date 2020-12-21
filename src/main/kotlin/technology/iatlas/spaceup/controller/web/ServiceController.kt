package technology.iatlas.spaceup.controller.web

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.WebNavigation
import technology.iatlas.spaceup.core.cmd.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.core.parser.ServiceParser
import technology.iatlas.spaceup.dto.Service
import technology.iatlas.spaceup.dto.ServiceOptions

@Controller("/services")
open class ServiceController(private val env: Environment) : BaseController() {
    private val log = LoggerFactory.getLogger(ServiceController::class.java)

    @WebNavigation("Services", "/services", prio = 2)
    @Get(produces = [MediaType.TEXT_HTML])
    @Cacheable("cache-service-list")
    open fun list(): String {
        val serviceOptions =
            listOf(ServiceOptions.START, ServiceOptions.STOP, ServiceOptions.RESTART)

        val cmd: MutableList<String> = mutableListOf("supervisorctl", "status")
        val services: List<Service>? = Runner<List<Service>>(env)
            .execute(Command(cmd), ServiceParser())

        return getRockerTemplate("views/services.rocker.html")
            .bind("services", services)
            .bind("options", serviceOptions)
            .render()
            .toString()
    }

    @Post("/execute/{servicename}/{option}", produces = [MediaType.TEXT_PLAIN])
    fun execute(servicename: String, option: ServiceOptions): HttpResponse<String> {
        log.debug("Execute $servicename: $option")

        val cmd: MutableList<String> = mutableListOf("supervisorctl",
            option.name, servicename)
        val response: String? = Runner<String>(env)
            .execute(Command(cmd), EchoParser())

        return HttpResponse.ok(response)
    }
}