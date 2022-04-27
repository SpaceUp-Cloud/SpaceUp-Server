package technology.iatlas.spaceup.controller.api

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.services.ProcessService

@Installed
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/process")
class ProcessController(
    private val processService: ProcessService
) {

    @Get("/proccess/{pid}", produces = [MediaType.TEXT_PLAIN])
    suspend fun getProgramForPid(pid: Int): HttpResponse<String> {
        return HttpResponse.ok(processService.getProgByProcess(pid))
    }
}