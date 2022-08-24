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
            val feedback = if(it.lowercase().contains("(started|stopped)".toRegex())) {
                Feedback(it, "")
            } else {
                Feedback("", it)
            }

            wsBroadcaster.broadcast(feedback, "feedback")
            executeServiceFeedback = feedback
        }

        serviceDeleteRunner.subject().subscribe {
            val feedback = if(it.lowercase().contains("error")) {
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