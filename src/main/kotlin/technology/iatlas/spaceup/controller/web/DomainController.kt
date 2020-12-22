package technology.iatlas.spaceup.controller.web

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.WebNavigation
import technology.iatlas.spaceup.dto.Domain
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.services.DomainService

@Controller("/domains")
open class DomainController(private val domainService: DomainService) : BaseController() {

    private val log = LoggerFactory.getLogger(DomainController::class.java)

    @WebNavigation("Domänen", "/domains", prio = 1)
    @Get(produces = [MediaType.TEXT_HTML])
    @Cacheable("cache-domain-list")
    open fun list(): String {

        val domains = domainService.list()

        return getRockerTemplate("views/domains.rocker.html")
            .bind("domains", domains)
            .render()
            .toString()
    }

    @Post(uri = "/add", produces = [MediaType.APPLICATION_JSON])
    fun add(@Body domains: List<Domain>): Map<String, Feedback> {
        log.info("Received list to add: $domains")

        return domainService.add(domains)
    }

    @Delete("/delete/{url}")
    fun delete(url: String): Feedback? {
        val domain = Domain(url)
        log.warn("Delete domain $domain")

        return domainService.delete(domain)
    }
}