package technology.iatlas.spaceup.controller.web

import com.fizzed.rocker.runtime.StringBuilderOutput
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.WebNavigation
import technology.iatlas.spaceup.services.DomainService

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/domains")
class DomainController(private val domainService: DomainService) : BaseController() {

    private val log = LoggerFactory.getLogger(DomainController::class.java)

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @WebNavigation("Domänen", "/domains", prio = 2)
    @Get(produces = [MediaType.TEXT_HTML])
    fun list(): String {

        return getRockerTemplate("views/domains.rocker.html")
            .bind("domains", domainService.list())
            .render(StringBuilderOutput.FACTORY)
            .toString()
    }

}