package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventPublisher
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.EchoParser
import technology.iatlas.spaceup.core.parser.ServiceParser
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.dto.Service
import technology.iatlas.spaceup.dto.ServiceOption
import technology.iatlas.spaceup.events.WebsocketFeedbackResponseEvent
import javax.inject.Singleton

@Singleton
class ServiceService(
    private val env: Environment,
    private val sshService: SshService,
    private val sseService: SseService<Feedback>,
    private val eventPublisher: ApplicationEventPublisher
    ) {

    /**
     * Get all service execution options
     */
    fun options(): List<ServiceOption> = ServiceOption.asList()

    /**
     * Returns a list of all existing services with their statuses
     */
    suspend fun list(): List<Service>? {
        val cmd: MutableList<String> = mutableListOf("supervisorctl", "status")
        val runner = Runner<List<Service>>(env, sshService)
        runner.execute(Command(cmd), ServiceParser())

        var services = emptyList<Service>()
        runner.subject().subscribe {
            services = it
        }

        return services
    }

    /**
     * Execute the service with a service option @see {ServiceOptions}
     *
     * @return Feedback
     * @see {Feedback}
     */
    suspend fun execute(serivce: Service, option: ServiceOption): Feedback {
        sseService.eventName="service exec"

        val cmd: MutableList<String> = mutableListOf("supervisorctl",
            option.name.toLowerCase(), serivce.name)

        val runner = Runner<String>(env, sshService)
        runner.execute(Command(cmd), EchoParser())

        var resultFeedback = Feedback("", "")

        runner.subject().subscribe {
            val feedback = if(it?.toLowerCase()!!.contains("(started|stopped)".toRegex())) {
                Feedback(it, "")
            } else {
                Feedback("", it)
            }

            eventPublisher.publishEvent(WebsocketFeedbackResponseEvent(feedback))
            sseService.publish(feedback)
            resultFeedback = feedback
        }

        return resultFeedback
    }
}