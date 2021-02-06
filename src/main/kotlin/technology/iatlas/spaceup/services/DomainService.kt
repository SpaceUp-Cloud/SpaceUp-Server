package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventPublisher
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.CreateDomainParser
import technology.iatlas.spaceup.core.parser.DeleteDomainParser
import technology.iatlas.spaceup.core.parser.DomainParser
import technology.iatlas.spaceup.dto.Domain
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.events.WebsocketFeedbackResponseEvent
import javax.inject.Singleton

@Singleton
class DomainService(
    private val env: Environment,
    private val sshService: SshService,
    private val sseService: SseService<Feedback>,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(DomainService::class.java)

    private val domainListRunner = Runner<List<Domain>>(env, sshService)
    private var domains = listOf<Domain>()

    private val addDomainRunner = Runner<Feedback>(env, sshService)
    private val deleteDomainRunner = Runner<Feedback>(env, sshService)

    init {
        // Cache domain list for faster access but delay on fresh data
        domainListRunner.subject().subscribe {
            domains = it
        }
    }

    suspend fun updateDomainList() {
        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "list")
        domainListRunner.execute(Command(cmd), DomainParser())
    }

    suspend fun add(domains: List<Domain>): List<Feedback> {
        sseService.eventName = "domain add"
        val feedbacks = linkedSetOf<Feedback>()

        domains.forEach { domain ->
            // Add domain to cmd
            val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "add")
            cmd.add(domain.url)

            addDomainRunner.execute(Command(cmd), CreateDomainParser())
            addDomainRunner.subject()
                .distinct()
                .subscribeBy(
                onNext = {
                    val info = it.info
                    val error = it.error

                    val infoResponse = Feedback(info = "${domain.url}: $info", error)
                    feedbacks.add(infoResponse)

                    eventPublisher.publishEvent(WebsocketFeedbackResponseEvent(infoResponse))
                    sseService.publish(infoResponse)
                },
                onError = { t ->
                    val error = t.message

                    val errorFeedback = Feedback("", "Could not add domain: $domain")
                    if (error != null) {
                        errorFeedback.error = error
                    }
                    eventPublisher.publishEvent(WebsocketFeedbackResponseEvent(errorFeedback))
                    sseService.publish(errorFeedback)
                },
                onComplete = {
                    log.debug("Finished 'Add domain'")
                }
            )

        }

        log.debug("Response: $feedbacks")
        return feedbacks.toMutableList()
    }

    suspend fun delete(domain: Domain): Feedback {
        sseService.eventName = "domain delete"
        val cmd = mutableListOf("uberspace", "web", "domain", "del", domain.url)

        deleteDomainRunner.execute(Command(cmd), DeleteDomainParser())
        var feedback = Feedback("", "")

        deleteDomainRunner.subject()
            .distinct()
            .subscribeBy(
                onNext = {
                    val f = Feedback("${domain.url}: ${it.info}", it.error)

                    eventPublisher.publishEvent(WebsocketFeedbackResponseEvent(f))
                    sseService.publish(f)
                    feedback = f
                },
                onError = { t ->
                    // Should not happened
                    log.error(t.message)
                    feedback = t.message?.let { it -> Feedback("", it) }!!

                    eventPublisher.publishEvent(WebsocketFeedbackResponseEvent(feedback))
                    sseService.publish(feedback)
                }
            )

        return feedback
    }

    /**
     * Get all domains
     */
    fun getDomainList(): List<Domain> {
        return domains
    }
}