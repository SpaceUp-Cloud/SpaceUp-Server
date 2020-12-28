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

    @WebNavigation("Domänen", "/domains", prio = 2)
    @Get(produces = [MediaType.TEXT_HTML])
    fun list(): String {

        val domains = domainService.list()

        return getRockerTemplate("views/domains.rocker.html")
            .bind("domains", domains)
            .render(StringBuilderOutput.FACTORY)
            .toString()
    }

}