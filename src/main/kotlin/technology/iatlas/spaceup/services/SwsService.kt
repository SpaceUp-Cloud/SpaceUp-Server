/*
 * Copyright (c) 2022 Thraax Session <spaceup@iatlas.technology>.
 *
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

import com.mongodb.client.MongoCollection
import io.micronaut.context.annotation.Context
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.simple.SimpleHttpResponseFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.eq
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.SpaceUpSftpConfig
import technology.iatlas.spaceup.config.SpaceupPathConfig
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.core.cmd.toFeedback
import technology.iatlas.spaceup.core.startup.createNormalizedPath
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.SftpFile
import technology.iatlas.spaceup.dto.db.Sws
import technology.iatlas.spaceup.isOk
import technology.iatlas.sws.SWS
import technology.iatlas.sws.objects.ParserException
import java.io.File

@Context
class SwsService(
    private val dbService: DbService,
    private val sshService: SshService,
    private val sftpConfig: SpaceUpSftpConfig,
    private val spaceupPathConfig: SpaceupPathConfig
) {
    private val log = LoggerFactory.getLogger(SwsService::class.java)
    private val swsCache = mutableListOf<SWS>()

    private var tempDir = spaceupPathConfig.temp

    private fun validateSWS(sws: Sws, feedback: Feedback) {
        log.info("Validate sws")
        // Create a temporary file
        "$tempDir/${sws.name}.sws".createNormalizedPath().toFile().apply {
            this.writeText(sws.content)
            try {
                SWS.createAndParse(this)
            } catch (ex: ParserException) {
                log.error(ex.message)
                feedback.error = "SWS content is corrupt! ${ex.message}"
            }
        }

    }

    private fun checkAndExecute(
        sws: Sws,
        swsRepo: MongoCollection<Sws>,
        executionName: String,
        executeCallback: (feedback: Feedback, exists: Boolean) -> Unit): Feedback {
        val feedback = Feedback("", "")

        // Check if sws is valid
        validateSWS(sws, feedback)
        if(!feedback.isOk()) {
            return feedback
        }

        val found = swsRepo.find(Sws::name eq sws.name).first() != null
        log.debug("${sws.name} found: $found")
        log.info("Will execute '$executionName' with server web script '${sws.name}'.")
        executeCallback(feedback, found)

        return feedback
    }

    fun create(sws: Sws): Feedback {
        log.info("Check if sws '${sws.name}' already exists.")
        val swsRepo = dbService.getRepo<Sws>()

        return checkAndExecute(sws, swsRepo, "create") { feedback, found ->
            val errorCase = "${sws.name} already exists!"

            // Should not exit && validation was fine
            if (!found && feedback.isOk()) {
                val result = swsRepo.insertOne(sws)
                if (result.wasAcknowledged()) {
                    feedback.info = "Created ${sws.name} successfully"
                    swsCache.add(sws.content.toSWS())
                } else {
                    feedback.error = errorCase
                }
            } else {
                feedback.error = errorCase
            }
        }
    }

    fun getAll(): List<Sws> {
        log.info("Get all SWS configurations")
        val swsRepo = dbService.getRepo<Sws>()

        // Find all SWS configurations
        val allSws = swsRepo.find()
        return allSws.toList()
    }

    fun update(sws: Sws): Feedback {
        log.info("Update ${sws.name} on database")
        val swsRepo = dbService.getRepo<Sws>()

        return checkAndExecute(sws, swsRepo, "update") { feedback, found ->
            val errorCase = "Could not update ${sws.name} as it does not exist!"

            if(found && feedback.isOk()) {
                val result = swsRepo.replaceOne(Sws::name eq sws.name, sws)
                if(result.wasAcknowledged() && result.matchedCount > 0) {
                    feedback.info = "updated ${sws.name} successfully"
                } else {
                    feedback.error = errorCase
                }
            } else {
                feedback.error = errorCase
            }
        }
    }

    fun delete(name: String): Feedback {
        log.info("Delete $name on database")
        val feedback = Feedback("", "")
        val errorCase = "Could not delete sws $name"

        val swsRepo = dbService.getRepo<Sws>()
        val result = swsRepo.deleteOne(Sws::name eq name)
        if(result.deletedCount == 1L && result.wasAcknowledged()) {
            feedback.info = "Deleted $name successfully"
        } else {
            feedback.error = errorCase
        }

        return feedback
    }

    fun updateCache() {
        val swsRepo = dbService.getRepo<Sws>()
        swsRepo.find().forEach { sws ->
            try {
                runBlocking {
                    launch {
                        log.debug("Add ${sws.name} to cache")
                        // If it throws an exception, the content is corrupt
                        swsCache.add(sws.content.toSWS())
                    }
                }
            }catch (ex: ParserException) {
                log.error(ex.message)
            }
        }
    }

    suspend fun execute(request: HttpRequest<*>): MutableHttpResponse<Feedback> {
        val path = request.path.split("/api/sws/custom")[1]
        val httpMethod = request.method
        val parameters: Map<String, List<String>> = request.parameters.asMap()
        val bodyOptional = request.body

        log.info("Execute SWS [$httpMethod] [$path] ${parameters.map { 
            if(it.key.lowercase() == "pass" || it.key.lowercase() == "password") 
                "${it.key}=[hidden]" 
            else "${it.key}=${it.value}"
        }}")

        // Handle HTTP request parameters
        val swsHttpParameters = mutableMapOf<String, Any?>()
        parameters.forEach { (t, u) ->
            try {
                // Values come always as String. We have to cast numbers to integers
                swsHttpParameters[t] = Integer.valueOf(u[0])
            } catch (ex: NumberFormatException) {
                swsHttpParameters[t] = u[0]
            }
        }

        // TODO: SU-19 Handle request body, important to transport secrets/credentials
        val body = mutableMapOf<String, Any?>()
        if(bodyOptional.isPresent) {
            // The body is a map of anything
            val tempBody = bodyOptional.get() as Map<*, *>
            tempBody.forEach { (k, v) ->
                if(k is String) {
                    body[k] = v
                } else {
                    log.warn("Key $k is not a string. Will be ignored!")
                }
            }
        }

        // Validate request to sws
        var feedback = Feedback("", "")
        // /<sws name>/<custom endpoint>
        lateinit var swsDb: Sws
        try {
            swsDb = dbService.getRepo<Sws>().find().first {
                // Here we can look for the correct sws template with the right name, the name is unique
                path.split("/")[1].replace("%20", " ").contains(it.name)
                        // and the correct http method request
                        && it.content.contains(httpMethod)
            }
        }catch (nsex: NoSuchElementException) {
            feedback.error = "Either HTTP method isn't supported for your request or SWS wasn't found"
            log.error(feedback.error)
            return SimpleHttpResponseFactory.INSTANCE.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE, feedback)
        }

        // Generate SWS
        var sws: SWS
        val file = "$tempDir/${swsDb.name}.sws".createNormalizedPath().toFile()
        file.bufferedWriter().use {
            it.write(swsDb.content)
        }.apply {
            sws = SWS.createAndParse(file, swsHttpParameters)
        }

        // Check if the request url matches with end
        if(!path.split(sws.name).first().replace("%20", " ").contains(sws.name)) {
            feedback.error = "Your sws url needs to begin with ${sws.name}.\n"
            feedback.error += "Example: /api/sws/custom/${sws.name}/<customendpoint>"
            log.error(feedback.error)
            return SimpleHttpResponseFactory.INSTANCE.status(HttpStatus.BAD_REQUEST, feedback)
        }

        // Actual execution
        var response: SshResponse
        val scriptname = "${sws.name.replace(" ", "_")}.sh"
        "$tempDir/$scriptname".createNormalizedPath().toFile().apply {
            this.writeText(sws.serverScript)

            val script = "${sftpConfig.remotedir}/$scriptname"
            val cmd = mutableListOf(
                // make script executable
                "chmod", "+x", script, ";",
                // if the server runs on Windows, we should convert the file to prevents issues
                "dos2unix", script, "2>/dev/null", ";",
                // ... then execute it
                script
            )

            // Upload + execute
            runBlocking {
                response = sshService.upload(
                    Command(cmd, SftpFile(scriptname, this@apply.toURI().toURL(),  true)))
            }
        }

        log.debug("Clear temporary files")

        feedback = response.toFeedback()
        return if(feedback.isOk()) {
            SimpleHttpResponseFactory.INSTANCE.ok(feedback)
        } else {
            SimpleHttpResponseFactory.INSTANCE.status(HttpStatus.INTERNAL_SERVER_ERROR, feedback)
        }

    }
}

fun String.toSWS(): SWS {
    var sws: SWS
    val tmpDir = System.getProperty("spaceup.tempdir")
    File("$tmpDir/sws-${(1..100).random()}.sws").apply {
        this.writeText(this@toSWS)
        sws = SWS.createAndParse(this, mutableMapOf())
    }
    return sws
}