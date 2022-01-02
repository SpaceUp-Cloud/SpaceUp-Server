package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import org.dizitart.no2.filters.Filter
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.exceptions.InstalledException
import technology.iatlas.spaceup.dto.Server
import technology.iatlas.spaceup.dto.Ssh
import technology.iatlas.spaceup.dto.User

@Context
class InstallerService(
    private val dbService: DbService
) {

    private val log = LoggerFactory.getLogger(InstallerService::class.java)

    /**
     * Generate an API key and set it in the database
     */
    fun generateAPIKey(): String {
        // Create API Key to validate it is the correct user
        val setupKey: String = List(8) {
            (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
        }.joinToString("")

        /*val md = MessageDigest.getInstance("SHA-256")
        val messageDigest = md.digest(setupKey.toByteArray())
        val num = BigInteger(1, messageDigest)
        var hashText = num.toString(16)
        while(hashText.length < 32) {
            hashText = "0$hashText"
        }

        return hashText*/

        return setupKey
    }

    fun getApiKey(): String {
        val db = dbService.getDb()
        val serverRepo = db.getRepository(Server::class.java)

        val servers = serverRepo.find().toList()
        if (servers.size > 1) throw InstalledException("Multiple contrains found! $servers")

        return servers.first().apiKey
    }

    fun createSshUser(ssh: Ssh): HttpResponse<String> {
        val db = dbService.getDb()
        val sshRepo = db.getRepository(Ssh::class.java)

        val findSshUsers = sshRepo.find {
            it.second.get("username") == ssh.username
        }

        if(!findSshUsers.isEmpty) {
            val errorExistingUser = "SSH ${ssh.username} already exists"
            log.error(errorExistingUser)
            return HttpResponse.badRequest(errorExistingUser)
        }

        val result = sshRepo.insert(ssh)
        if(result.affectedCount == 1) {
            log.info("SSH User ${ssh.username} was created")
            return HttpResponse.ok("User successfully created!")
        }

        return HttpResponse.notAllowed()
    }

    fun createUser(user: User): HttpResponse<String> {
        val db = dbService.getDb()
        val userRepo = db.getRepository(User::class.java)
        val findUser = userRepo.find {
            it.second.get("username") == user.username
        }

        if(!findUser.isEmpty) {
            val errorExistingUser = "User ${user.username} exists already!"
            log.error(errorExistingUser)
            return HttpResponse.badRequest(errorExistingUser)
        }

        val result = userRepo.insert(user)
        if(result.affectedCount == 1) {
            log.info("User ${user.username} was created")
            return HttpResponse.ok("User successfully created!")
        }

        return HttpResponse.notAllowed()
    }

    fun finalizeInstallation(): HttpResponse<String> {
        getAllSteps().map {
            val result = this.validateStep(it)
            if(!result) {
                return HttpResponse.status(HttpStatus.EXPECTATION_FAILED, "Unfinished step '$it'")
            }
        }

        // Seems to be fine everything
        // Set installation to be done
        val db = dbService.getDb()
        val serverRepo = db.getRepository(Server::class.java)

        val serverDto = Server(true, "")
        serverRepo.update(Filter.ALL, serverDto)

        val finishMsg = "Installation done. Set system to state 'installed'"
        log.info(finishMsg)
        return HttpResponse.ok(finishMsg)
    }

    /**
     * Validate if a necessary installation step was ran
     */
    private fun validateStep(step: InstallSteps): Boolean {
        val db = dbService.getDb()

        when (step) {
            InstallSteps.CREATE_USER -> {
                val userRepo = db.getRepository(User::class.java)
                return !userRepo.find().isEmpty
            }
            InstallSteps.CREATE_SSHUSER -> {
                val sshRepo = db.getRepository(Ssh::class.java)
                return !sshRepo.find().isEmpty
            }
            else -> {
                return false
            }
        }
    }

    fun getAllSteps(): List<InstallSteps> {
        return InstallSteps.values().toList()
    }
}

enum class InstallSteps() {
    CREATE_USER, CREATE_SSHUSER
}