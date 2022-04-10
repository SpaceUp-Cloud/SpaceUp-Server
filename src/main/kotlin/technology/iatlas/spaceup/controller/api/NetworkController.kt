package technology.iatlas.spaceup.controller.api

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import technology.iatlas.spaceup.core.annotations.Installed
import technology.iatlas.spaceup.dto.NetworkProgram
import technology.iatlas.spaceup.services.NetworkService

@Installed
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api/network")
class NetworkController(
    private val networkService: NetworkService,
) {

    /**
     * Read actively listening programs on network
     * @see NetworkProgram
     */
    @Get("/read/programs")
    suspend fun readListening(): HttpResponse<List<NetworkProgram>> {
        return HttpResponse.ok(networkService.readListeningPrograms())
    }

    @Get("/proccess/{pid}", produces = [MediaType.TEXT_PLAIN])
    suspend fun getProgramForPid(pid: Int): HttpResponse<String> {
        return HttpResponse.ok(networkService.getProcess(pid))
    }
}