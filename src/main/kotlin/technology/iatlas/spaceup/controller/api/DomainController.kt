package technology.iatlas.spaceup.controller.api

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Domain
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.services.DomainService
import technology.iatlas.spaceup.services.SseService

@Controller("/api/domain")
class DomainController(private val domainService: DomainService) {
    private val log = LoggerFactory.getLogger(DomainController::class.java)

    @Post(uri = "/add", produces = [MediaType.APPLICATION_JSON])
    fun add(@Body domains: List<Domain>): Map<String, Feedback> {
        log.info("Received list to add: $domains")

        return domainService.add(domains)
    }

    @Delete("/delete/{url}")
    fun delete(url: String): Feedback {
        val domain = Domain(url)
        log.warn("Delete domain $domain")

        return domainService.delete(domain)
    }
}