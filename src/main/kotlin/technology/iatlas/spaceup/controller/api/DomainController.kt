package technology.iatlas.spaceup.controller.api

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Domain
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.services.DomainService
import java.util.*
import javax.annotation.Nullable

@Controller("/api/domain")
class DomainController(private val domainService: DomainService) {
    private val log = LoggerFactory.getLogger(DomainController::class.java)

    /**
     * Add a new domain
     *
     * @return Map<String, Feedback> - per domain a feedback
     */
    @Post(uri = "/add", produces = [MediaType.APPLICATION_JSON])
    suspend fun add(@Body domains: List<Domain>): HttpResponse<List<Feedback>> {
        log.info("Received list to add: $domains")

        return HttpResponse.ok(domainService.add(domains))
    }

    /**
     * Delete a domain with the corresponded url
     * @return Feedback
     */
    @Delete("/delete/{url}")
    suspend fun delete(url: String): HttpResponse<Feedback> {
        val domain = Domain(url)
        log.warn("Delete domain $domain")

        return HttpResponse.ok(domainService.delete(domain))
    }

    /**
     * Get a list of domains
     * @return List<Domain> - list of domains
     */
    @Get(uri = "/list{?cached}", produces = [MediaType.APPLICATION_JSON])
    suspend fun list(cached: Optional<Boolean>): List<Domain> {
        log.debug("Get domain list.")

        if(get(cached).equals(false)) {
            log.info("Get updated domain list.")
            domainService.updateDomainList()
        }
        return domainService.list()
    }

    private fun get(myOptional: Optional<Boolean>): Boolean {
        return myOptional.orElse(false)
    }

}