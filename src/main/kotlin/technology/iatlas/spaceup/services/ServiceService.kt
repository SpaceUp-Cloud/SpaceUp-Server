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
import io.micronaut.core.io.ResourceLoader
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.config.SpaceUpSftpConfig
import technology.iatlas.spaceup.config.SpaceupPathConfig
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.core.parser.LogsParser
import technology.iatlas.spaceup.core.parser.ServiceParser
import technology.iatlas.spaceup.dto.*

@Context
class ServiceService(
    sshService: SshService,
    private val config: SpaceupPathConfig,
    private val sftpConfig: SpaceUpSftpConfig,
    private val wsBroadcaster: WsBroadcaster,
    private val resourceLoader: ResourceLoader
    ) : WsServiceInf {

    private val log = LoggerFactory.getLogger(ServiceService::class.java)

    override val topic = "services"

    private val services: MutableList<Service> = mutableListOf()

    private var executeServiceFeedback: Feedback = Feedback("", "")
    private lateinit var deleteServiceFeedback: Feedback
    private lateinit var currentLogs: Log

    private val serviceListRunner = Runner<List<Service>>(sshService)
    private val serviceExecRunner = Runner<String>(sshService)
    private val serviceDeleteRunner = Runner<String>(sshService)
    private val serviceLogRunner = Runner<Log>(sshService)

    init {
        serviceListRunner.subject().subscribe {
            // Broadcast service list
            wsBroadcaster.broadcast(it, topic)

            services.clear()
            services.addAll(it)
        }

        serviceExecRunner.subject().subscribe {
            val feedback = if(it.lowercase()!!.contains("(started|stopped)".toRegex())) {
                Feedback(it, "")
            } else {
                Feedback("", it)
            }

            wsBroadcaster.broadcast(feedback, "feedback")
            executeServiceFeedback = feedback
        }

        serviceDeleteRunner.subject().subscribe {
            val feedback = if(it.lowercase()!!.contains("error")) {
                Feedback("", it)
            } else {
                Feedback(it, "")
            }

            wsBroadcaster.broadcast(feedback, "feedback")
            deleteServiceFeedback = feedback
        }

        serviceLogRunner.subject().subscribe {
            wsBroadcaster.broadcast(it, topic)
            currentLogs = it
        }
    }

    /**
     * Get all service execution options
     */
    fun options(): List<ServiceOption> = ServiceOption.asList()

    /**
     * Returns a list of all existing services with their statuses
     */
    suspend fun list(): List<Service> {
        val cmd: MutableList<String> = mutableListOf("supervisorctl", "status")

        serviceListRunner.execute(Command(cmd), ServiceParser())
        return services
    }

    /**
     * Execute the service with a service option {@link ServiceOption}
     *
     * @return Feedback
     * @see Feedback
     */
    suspend fun execute(serivce: Service, option: ServiceOption): Feedback {
        val cmd: MutableList<String> = mutableListOf("supervisorctl",
            option.name.lowercase(), serivce.name)

        serviceExecRunner.execute(Command(cmd), EchoParser())
        return executeServiceFeedback
    }

    suspend fun getLogs(servicename: String, type: Logtype, limits: Int, isReversed: Boolean): Logfile {
        val logsScript = resourceLoader.getResource("commands/getLogs.sh")
        val remotefile = sftpConfig.remotedir + "/getLogs.sh"

        val reversed = if (isReversed)  "--reversed" else ""
        val cmd: MutableList<String> = mutableListOf(
            "bash", remotefile,
            "-b", config.logs,
            "-s", servicename,
            "-t", type.name,
            "-l", limits.toString(),
            reversed,
        )
        val sftpFile = SftpFile("getLogs.sh", logsScript.get(), execute = true)
        serviceLogRunner.execute(Command(cmd, sftpFile), LogsParser())

        return Logfile(currentLogs)
    }
    /**
     * TODO: Finish implementation of deleting service
     */
    suspend fun deleteService(servicename: String, servicePath: String): Feedback {
        val deleteScript = resourceLoader.getResource("/commands/deleteService.sh")
        val cmd: MutableList<String> = mutableListOf("bash", deleteScript.get().file, servicePath, servicename)

        serviceDeleteRunner.execute(Command(cmd), EchoParser())

        return Feedback(cmd.toString(), "Not implemented yet")
    }
}