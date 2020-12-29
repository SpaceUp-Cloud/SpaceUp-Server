package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.core.parser.ServiceParser
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.Service
import technology.iatlas.spaceup.dto.ServiceOption
import javax.inject.Singleton

@Singleton
class ServiceService(
    private val env: Environment,
    private val sshService: SshService,
    private val sseService: SseService<Feedback>
    ) {

    /**
     * Get all service execution options
     */
    fun options(): List<ServiceOption> = ServiceOption.asList()

    /**
     * Returns a list of all existing services with their statuses
     */
    fun list(): List<Service>? {
        val cmd: MutableList<String> = mutableListOf("supervisorctl", "status")
        return Runner<List<Service>>(env, sshService)
            .execute(Command(cmd), ServiceParser())
    }

    /**
     * Execute the service with a service option @see {ServiceOptions}
     *
     * @return Feedback
     * @see {Feedback}
     */
    fun execute(serivce: Service, option: ServiceOption): Feedback {
        sseService.eventName="service exec"

        val cmd: MutableList<String> = mutableListOf("supervisorctl",
            option.name.toLowerCase(), serivce.name)

        val result: String? = Runner<String>(env, sshService)
            .execute(Command(cmd), EchoParser())

        val feedback = if(result?.toLowerCase()!!.contains("(started|stopped)".toRegex())) {
            Feedback(result, "")
        } else {
            Feedback("", result)
        }

        sseService.publish(feedback)
        return feedback
    }
}