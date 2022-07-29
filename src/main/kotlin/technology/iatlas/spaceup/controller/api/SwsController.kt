package technology.iatlas.spaceup.controller.api

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.db.Sws
import technology.iatlas.spaceup.services.SwsService
import technology.iatlas.spaceup.toHttpResponse

@Installed
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/sws")
class SwsController(
    private val swsService: SwsService
) {
    private val log = LoggerFactory.getLogger(SwsController::class.java)

    @Post(uri = "/create/{name}", produces = [MediaType.APPLICATION_JSON], consumes = [MediaType.TEXT_PLAIN])
    suspend fun create(@PathVariable name: String, @Body swsContent: String): HttpResponse<Feedback> {
        val sws = Sws(name, swsContent)
        log.info("Create SWS")
        log.debug("$sws")
        return swsService.create(sws).toHttpResponse()
    }

    @Get(uri = "/all", produces = [MediaType.APPLICATION_JSON])
    suspend fun getAllSws(): HttpResponse<List<Sws>> {
        log.info("Retrieve all sws configurations")
        val allSws = swsService.getAll()
        return HttpResponse.ok(allSws)
    }

    @Put(uri = "/update/{name}", produces = [MediaType.APPLICATION_JSON], consumes = [MediaType.TEXT_PLAIN])
    suspend fun updateSws(@PathVariable name: String, @Body swsContent: String): HttpResponse<Feedback> {
        val sws = Sws(name, swsContent)

        log.info("Update with $sws")
        return swsService.update(sws).toHttpResponse()
    }

    @Delete("/delete/{name}", produces = [MediaType.APPLICATION_JSON])
    suspend fun deleteSws(@PathVariable name: String): HttpResponse<Feedback> {
        log.info("Delete sws $name")
        return swsService.delete(name).toHttpResponse()
    }
}