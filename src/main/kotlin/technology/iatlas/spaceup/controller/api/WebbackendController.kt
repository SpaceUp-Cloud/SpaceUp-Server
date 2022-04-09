package technology.iatlas.spaceup.controller.api

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.WebbackendCmd
import technology.iatlas.spaceup.dto.WebbackendConfiguration
import technology.iatlas.spaceup.services.WebbackendService

@Installed
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/web/backend")
class WebbackendController(
    private val webbackendService: WebbackendService
) {

    /**
     * Gather the 'uberspace web backend list' configuration
     *
     * @see WebbackendConfiguration
     * @return WebbackendConfiguration - the web backend configuration
     */
    @Get("/read", processes = [MediaType.APPLICATION_JSON])
    suspend fun read(): HttpResponse<List<WebbackendConfiguration>> {
        return HttpResponse.ok(webbackendService.read())
    }

    /**
     * Create a new web backend configuration with the 'uberspace web backend set' command.
     *
     * @return Feedback - the feedback object
     * @see Feedback
     */
    @Post("/create", processes = [MediaType.APPLICATION_JSON])
    suspend fun create(@Body webbackendCmd: WebbackendCmd): HttpResponse<Feedback> {
        return HttpResponse.ok(webbackendService.create(webbackendCmd))
    }

    /**
     * Delete the web backend configuration for a specific domain or url with 'uberspace web backend del param'.
     *
     * @return Feedback - the feedback object
     * @see Feedback
     */
    @Delete("/delete/{domain}")
    suspend fun delete(domain: String): HttpResponse<Feedback> {
        return HttpResponse.ok(webbackendService.delete(domain))
    }
}