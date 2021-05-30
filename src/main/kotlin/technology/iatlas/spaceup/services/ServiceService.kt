package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.core.parser.ServiceParser
import technology.iatlas.spaceup.dto.*
import java.io.File
import java.net.URI
import javax.inject.Singleton

@Singleton
class ServiceService(
    private val env: Environment,
    private val sshService: SshService,
    private val wsBroadcaster: WsBroadcaster
    ) : WsServiceInf {

    override val topic = "services"

    private val services: MutableList<Service> = mutableListOf()

    private var executeServiceFeedback: Feedback = Feedback("", "")
    private lateinit var deleteServiceFeedback: Feedback

    private val serviceListRunner = Runner<List<Service>>(env, sshService)
    private val serviceExecRunner = Runner<String>(env, sshService)
    private val serviceDeleteRunner = Runner<String>(env, sshService)

    init {
        serviceListRunner.subject().subscribe {
            // Broadcast service list
            wsBroadcaster.broadcast(it, topic)

            services.clear()
            services.addAll(it)
        }

        serviceExecRunner.subject().subscribe {
            val feedback = if(it?.toLowerCase()!!.contains("(started|stopped)".toRegex())) {
                Feedback(it, "")
            } else {
                Feedback("", it)
            }

            wsBroadcaster.broadcast(feedback, "feedback")
            executeServiceFeedback = feedback
        }

        serviceDeleteRunner.subject().subscribe {
            val feedback = if(it?.toLowerCase()!!.contains("error")) {
                Feedback("", it)
            } else {
                Feedback(it, "")
            }

            wsBroadcaster.broadcast(feedback, "feedback")
            deleteServiceFeedback = feedback
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
            option.name.toLowerCase(), serivce.name)

        serviceExecRunner.execute(Command(cmd), EchoParser())
        return executeServiceFeedback
    }

    fun getLogs(servicename: String, limits: Int): Map<Logtype, Logfile> {
        val logMap = mutableMapOf<Logtype, Logfile>()
        // /home/<user>/logs/<service>/<service>.log
        //                        ... /<service>-error.log
        val basePath = listOf(System.getProperty("user.home"), "logs", servicename).joinToString("/")

        val logUri = URI("$basePath/$servicename.log").normalize()
        val uriLog = File(logUri).readText()
        logMap[Logtype.stdout] = Logfile(logUri.path, uriLog)

        val errorUri = URI("$basePath/$servicename-error.log").normalize()
        val uriErrorLog = File(errorUri).readText()
        logMap[Logtype.stderr] = Logfile(errorUri.path, uriErrorLog)

        return logMap
    }

    fun getLogs(servicename: String, type: Logtype, limits: Int): Logfile {
        TODO("Not implemented yet")
    }

    /**
     * TODO: Finish implementation of deleting service
     */
    suspend fun deleteService(servicename: String, servicePath: String): Feedback {
        val deleteScript = this.javaClass.getResource("/commands/deleteService.sh")
        val cmd: MutableList<String> = mutableListOf("bash", deleteScript.file, servicePath, servicename)

        serviceDeleteRunner.execute(Command(cmd), EchoParser())

        return Feedback(cmd.toString(), "Not implemented yet")
    }
}