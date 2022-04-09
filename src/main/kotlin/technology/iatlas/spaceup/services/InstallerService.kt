/*
 * Copyright (c) 2022 Gino Atlas.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package technology.iatlas.spaceup.services

import io.micronaut.context.annotation.Context
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.setValue
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.exceptions.InstalledException
import technology.iatlas.spaceup.dto.db.Server
import technology.iatlas.spaceup.dto.db.Ssh
import technology.iatlas.spaceup.dto.db.User

@Context
class InstallerService(
    private val dbService: DbService,
    private val securityService: SecurityService
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

        return setupKey
    }

    fun getApiKey(): String {
        val db = dbService.getDb()
        val serverRepo = db.getCollection<Server>()

        val servers = serverRepo.find().toList()
        if (servers.size > 1) throw InstalledException("Multiple contrains found! $servers")

        return servers.first().apiKey
    }

    fun createSshUser(ssh: Ssh): HttpResponse<String> {
        val db = dbService.getDb()
        val sshRepo = db.getCollection<Ssh>()
        val sshUser = sshRepo.findOne(Ssh::username eq ssh.username)

        if(sshUser != null) {
            val errorExistingUser = "SSH ${ssh.username} already exists"
            log.error(errorExistingUser)
            return HttpResponse.badRequest(errorExistingUser)
        } else {
            securityService.encrypt(ssh)

            val result = sshRepo.insertOne(ssh)
            if(result.wasAcknowledged()) {
                log.info("SSH User ${ssh.username} was created.")
                return HttpResponse.ok("User successfully created!")
            }
            log.error("Could not create $ssh")
            return HttpResponse.badRequest("User ${ssh.username} was not created")
        }
    }

    fun createUser(user: User): HttpResponse<String> {
        val db = dbService.getDb()
        val userRepo = db.getCollection<User>()
        val findUser = userRepo.findOne(User::username eq user.username)

        if(findUser != null) {
            val errorExistingUser = "User ${user.username} exists already!"
            log.error(errorExistingUser)
            return HttpResponse.badRequest(errorExistingUser)
        } else {
            securityService.encrypt(user)

            val result = userRepo.insertOne(user)
            if(result.wasAcknowledged()) {
                log.info("User ${user.username} was created.")
                return HttpResponse.ok("User successfully created!")
            }
            log.error("Could not create $user")
            return HttpResponse.badRequest("User ${user.username} was not created")
        }
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
        val serverRepo = db.getCollection<Server>()
        serverRepo.updateOne(Server::installed eq false, setValue(Server::installed, true))

        val finishMsg = "Installation done. Set system to state 'installed'"
        log.info(finishMsg)
        return HttpResponse.ok(finishMsg)
    }

    /**
     * Validate if a necessary installation step was ran
     */
    private fun validateStep(step: InstallSteps): Boolean {
        val db = dbService.getDb()

        return when (step) {
            InstallSteps.CREATE_USER -> {
                val userRepo = db.getCollection<User>()
                userRepo.findOne() != null
            }
            InstallSteps.CREATE_SSHUSER -> {
                val sshRepo = db.getCollection<Ssh>()
                sshRepo.findOne() != null
            }
        }
    }

    private fun getAllSteps(): List<InstallSteps> {
        return InstallSteps.values().toList()
    }
}

enum class InstallSteps {
    CREATE_USER, CREATE_SSHUSER
}