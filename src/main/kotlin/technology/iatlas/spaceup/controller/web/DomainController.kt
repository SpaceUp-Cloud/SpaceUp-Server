package technology.iatlas.spaceup.controller.web

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.ClientLink
import technology.iatlas.spaceup.core.cmd.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.CreateDomainParser
import technology.iatlas.spaceup.core.parser.DeleteDomainParser
import technology.iatlas.spaceup.core.parser.DomainParser
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.dto.Domains
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.services.RouterService

@Controller("/domains")
open class DomainController(private val env: Environment, routerService: RouterService) :
    BaseController(routerService) {

    private val log = LoggerFactory.getLogger(DomainController::class.java)

    @ClientLink("Domänen")
    @Get(produces = [MediaType.TEXT_HTML])
    @Cacheable("cache-domain-list")
    open fun list(): String {

        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "list")
        val domains: Domains? = Runner<Domains>(env).execute(Command(cmd), DomainParser())

        return getRockerTemplate("views/domains.rocker.html")
            .bind("domains", domains)
            .render()
            .toString()
    }

    @Post(uri = "/add", produces = [MediaType.APPLICATION_JSON])
    fun add(@Body data: Domains): Map<String, Feedback> {
        log.info("Received list to add: $data")

        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "add")
        val feedbacks: MutableMap<String, Feedback> = mutableMapOf()

        data.domains.forEach { domain ->
            // Add domain to cmd
            cmd.add(domain)
            val response: Feedback? = Runner<Feedback>(env)
                .execute(Command(cmd), CreateDomainParser())

            feedbacks[domain] = response!!

            // Remove domain from cmd
            cmd.remove(domain)
        }

        log.debug("Response: $feedbacks")
        return feedbacks
    }

    @Delete("/delete/{domain}")
    fun delete(domain: String): HttpResponse<Feedback> {
        log.warn("Delete domain $domain")

        val cmd = mutableListOf("uberspace", "web", "domain", "del", domain)

            val response: Feedback? = Runner<Feedback>(env)
                .execute(Command(cmd), DeleteDomainParser())

        return HttpResponse.ok(response)
    }
}