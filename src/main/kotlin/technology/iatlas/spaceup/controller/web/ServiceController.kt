package technology.iatlas.spaceup.controller.web

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.env.Environment
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import technology.iatlas.spaceup.core.annotations.ClientLink
import technology.iatlas.spaceup.core.cmd.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.ServiceParser
import technology.iatlas.spaceup.dto.Service
import technology.iatlas.spaceup.services.ClassService

@Controller("/Services")
open class ServiceController(private val env: Environment, classService: ClassService) : BaseController(classService) {

    @ClientLink("Services")
    @Get(produces = [MediaType.TEXT_HTML])
    @Cacheable("cache-service-list")
    open fun list(): String {

        val cmd: MutableList<String> = mutableListOf("supervisorctl", "status")
        val services: List<Service>? = Runner<List<Service>>(env)
            .execute(Command(cmd), ServiceParser())

        return getRockerTemplate("views/services.rocker.html")
            .bind("services", services)
            .render()
            .toString()
    }
}