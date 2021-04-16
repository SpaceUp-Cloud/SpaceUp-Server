package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.runtime.StringBuilderOutput
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.WebNavigation
import technology.iatlas.spaceup.services.DomainService

@Controller("/domains")
class DomainController(private val domainService: DomainService) : BaseController() {

    private val log = LoggerFactory.getLogger(DomainController::class.java)

    @WebNavigation("Domänen", "/domains", prio = 2)
    @Get(produces = [MediaType.TEXT_HTML])
    fun list(): String {

        return getRockerTemplate("views/domains.rocker.html")
            .bind("domains", domainService.list())
            .render(StringBuilderOutput.FACTORY)
            .toString()
    }

}