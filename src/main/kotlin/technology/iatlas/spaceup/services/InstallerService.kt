/*
 * Copyright (c) 2022 spaceup@iatlas.technology.
 * SpaceUp-Server is free software; You can redistribute it and/or modify it under the terms of:
 *   - the GNU Affero General Public License version 3 as published by the Free Software Foundation.
 * You don't have to do anything special to accept the license and you don’t have to notify anyone which that you have made that decision.
 *
 * SpaceUp-Server is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See your chosen license for more details.
 *
 * You should have received a copy of both licenses along with SpaceUp-Server
 * If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * There is a strong belief within us that the license we have chosen provides not only the best solution for providing you with the essential freedom necessary to use SpaceUp-Server within your projects, but also for maintaining enough copyleft strength for us to feel confident and secure with releasing our hard work to the public. For your convenience we've included our own interpretation of the license we chose, which can be seen below.
 *
 * Our interpretation of the GNU Affero General Public License version 3: (Quoted words are words in which there exists a definition within the license to avoid ambiguity.)
 *   1. You must always provide the source code, copyright and license information of SpaceUp-Server whenever you "convey" any part of SpaceUp-Server;
 *      be it a verbatim copy or a modified copy.
 *   2. SpaceUp-Server was developed as a library and has therefore been designed without knowledge of your work; as such the following should be implied:
 *      a) SpaceUp-Server was developed without knowledge of your work; as such the following should be implied:
 *         i)  SpaceUp-Server should not fall under a work which is "based on" your work.
 *         ii) You should be free to use SpaceUp-Server in a work covered by the:
 *             - GNU General Public License version 2
 *             - GNU Lesser General Public License version 2.1
 *             This is due to those licenses classifying SpaceUp-Server as a work which would fall under an "aggregate" work by their terms and definitions;
 *             as such it should not be covered by their terms and conditions. The relevant passages start at:
 *             - Line 129 of the GNU General Public License version 2
 *             - Line 206 of the GNU Lesser General Public License version 2.1
 *      b) If you have not "modified", "adapted" or "extended" SpaceUp-Server then your work should not be bound by this license,
 *         as you are using SpaceUp-Server under the definition of an "aggregate" work.
 *      c) If you have "modified", "adapted" or "extended" SpaceUp-Server then any of those modifications/extensions/adaptations which you have made
 *         should indeed be bound by this license, as you are using SpaceUp-Server under the definition of a "based on" work.
 *
 * Our hopes is that our own interpretation of license aligns perfectly with your own values and goals for using our work freely and securely. If you have any questions at all about the licensing chosen for SpaceUp-Server you can email us directly at spaceup@iatlas.technology or you can get in touch with the license authors (the Free Software Foundation) at licensing@fsf.org to gain their opinion too.
 *
 * Alternatively you can provide feedback and acquire the support you need at our support forum. We'll definitely try and help you as soon as possible, and to the best of our ability; as we understand that user experience is everything, so we want to make you as happy as possible! So feel free to get in touch via our support forum and chat with other users of SpaceUp-Server here at:
 * https://spaceup.iatlas.technology
 *
 * Thanks, and we hope you enjoy using SpaceUp-Server and that it's everything you ever hoped it could be.
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