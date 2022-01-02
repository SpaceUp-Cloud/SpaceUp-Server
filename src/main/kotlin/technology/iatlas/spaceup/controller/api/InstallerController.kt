package technology.iatlas.spaceup.controller.api

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.Installation
import technology.iatlas.spaceup.dto.Ssh
import technology.iatlas.spaceup.dto.User
import technology.iatlas.spaceup.services.DbService
import technology.iatlas.spaceup.services.InstallerService

/**
 * This is the installer endpoint to create a proper application setup.
 * The endpoint will only be reachable when there isn't an installation.
 */
@Installation
@Controller("/api/installer")
class InstallerController(
    private val dbService: DbService,
    private val installerService: InstallerService
) {
    private val log = LoggerFactory.getLogger(InstallerController::class.java)
    private val steps = installerService.getAllSteps()
    /**
     * Create a new user for authentication on SpaceUp
     * @param User defines a user database object
     */
    @Post(uri = "/step/createUser", processes = [MediaType.APPLICATION_JSON])
    fun createUser(@Body user: User, @Header("X-SpaceUp-Key") apiKey: String): HttpResponse<String> {
        // TODO 29.12.2010: Create user service for reusage

        if(installerService.getApiKey() != apiKey) {
            return HttpResponse.status(HttpStatus.EXPECTATION_FAILED, "API Key is not valid!")
        }

        return installerService.createUser(user)
    }

    /**
     * Create the ssh user for authentication via SSH
     * @param SshUser defines ssh user database object
     */
    @Post(uri = "/step/createSshSetup", processes = [MediaType.APPLICATION_JSON])
    fun createSshUser(@Body ssh: Ssh, @Header("X-SpaceUp-Key") apiKey: String): HttpResponse<String> {

        if(installerService.getApiKey() != apiKey) {
            return HttpResponse.status(HttpStatus.EXPECTATION_FAILED, "API Key is not valid!")
        }

        return installerService.createSshUser(ssh)
    }

    /**
     * This is the final step to finalize the installation. It needs to be run.
     */
    @Post(uri = "/step/final")
    suspend fun getFinalization(@Header("X-SpaceUp-Key") apiKey: String): HttpResponse<String>{
        val responses = mutableListOf<HttpResponse<String>>()

        if(installerService.getApiKey() != apiKey) {
            return HttpResponse.status(HttpStatus.EXPECTATION_FAILED, "API Key is invalid")
        }

        return installerService.finalizeInstallation()
    }
}