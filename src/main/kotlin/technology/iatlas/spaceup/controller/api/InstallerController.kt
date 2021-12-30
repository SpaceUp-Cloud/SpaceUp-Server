package technology.iatlas.spaceup.controller.api

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.annotations.Installation
import technology.iatlas.spaceup.dto.Server
import technology.iatlas.spaceup.dto.Ssh
import technology.iatlas.spaceup.dto.User
import technology.iatlas.spaceup.services.DbService

/**
 * This is the installer endpoint to create a proper application setup.
 * The endpoint will only be reachable when there isn't an installation.
 */
@Installation
@Controller("/api/installer")
class InstallerController(
    private val dbService: DbService
) {
    private val log = LoggerFactory.getLogger(InstallerController::class.java)
    private val steps = mutableMapOf<String, Boolean>()

    init {
        steps["createUser"] = false
        steps["createSshSetup"] = false
    }

    /**
     * Create a new user for authentication on SpaceUp
     * @param User defines a user database object
     */
    @Post(uri = "/step/createUser")
    fun createUser(@Body user: User): HttpResponse<String> {
        // TODO 29.12.2010: Create user service for reusage
        val db = dbService.getDb()
        val userRepo = db.getRepository(User::class.java)
        val result = userRepo.insert(user)

        if(result.affectedCount > userRepo.size()) {
            log.info("User ${user.username} was created")
            steps["createUser"] = true
            return HttpResponse.ok("User successful created!")
        }

        steps["createUser"] = false
        return HttpResponse.notAllowed()
    }

    /**
     * Create the ssh user for authentication via SSH
     * @param SshUser defines ssh user database object
     */
    @Post(uri = "/step/createSshSetup")
    fun createSshUser(@Body ssh: Ssh): HttpResponse<String> {

        val db = dbService.getDb()
        val sshRepo = db.getRepository(Ssh::class.java)
        val result = sshRepo.insert(ssh)

        if(result.affectedCount > sshRepo.size()) {
            steps["createSshSetup"] = true
            return HttpResponse.ok("User successful created!")
        }

        steps["createSshSetup"] = false
        return HttpResponse.notAllowed()
    }

    /**
     * This is the final step to finalize the installation. It needs to be run.
     */
    @Post(uri = "/step/final")
    fun getFinalization(): MutableList<HttpResponse<String>> {
        val responses = mutableListOf<HttpResponse<String>>()
        steps.forEach { (t, u) ->
            if(!u) {
                responses.add(
                    HttpResponse.status(
                        HttpStatus.EXPECTATION_FAILED,
                        "Step '$t' was not processed!"
                    )
                )
            }
        }

        // Seems to be fine everything
        if(responses.size == 0) {
            // Set installation to be done
            val db = dbService.getDb()
            val serverRepo = db.getRepository(Server::class.java)

            val serverDto = Server(true)
            serverRepo.update(serverDto)

            responses.add(HttpResponse.ok("Installation done. Set system to state 'installed'"))
        }

        return responses
    }
}