package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventPublisher
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.CreateDomainParser
import technology.iatlas.spaceup.core.parser.DeleteDomainParser
import technology.iatlas.spaceup.core.parser.DomainParser
import technology.iatlas.spaceup.dto.Domain
import technology.iatlas.spaceup.dto.Feedback
import technology.iatlas.spaceup.events.WebsocketResponseEvent
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

    init {
        // Cache domain list for faster access but delay on fresh data
        domainListRunner.getBehaviourSubject().subscribe {
            domains = it
        }
    }

    suspend fun updateDomainList() {
        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "list")
        domainListRunner.execute(Command(cmd), DomainParser())
    }

    suspend fun add(domains: List<Domain>): List<Feedback> {
        sseService.eventName = "domain add"
        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "add")
        val feedbacks = linkedSetOf<Feedback>()

        domains.forEach { domain ->
            // Add domain to cmd
            cmd.add(domain.url)
            addDomainRunner.execute(Command(cmd), CreateDomainParser())

            addDomainRunner.getBehaviourSubject()
                .doOnError { t ->
                    val error = t.message
                    val errorFeedback = error?.let { Feedback("", it) }

                    eventPublisher.publishEvent(WebsocketResponseEvent(errorFeedback))
                    if (errorFeedback != null) {
                        sseService.publish(errorFeedback)
                    }
                }
                .subscribe {
                    when {
                        it?.info!!.isNotEmpty() -> {
                            val infoResponse = Feedback(info = "${domain.url}: ${it.info}", "")
                            feedbacks.add(infoResponse)

                            eventPublisher.publishEvent(WebsocketResponseEvent(infoResponse))
                            sseService.publish(infoResponse)
                        }
                        it.error.isNotEmpty() -> {
                            val errorResponse = Feedback(info = "", error = "${domain.url}: ${it.error}")
                            feedbacks.add(errorResponse)

                            eventPublisher.publishEvent(WebsocketResponseEvent(errorResponse))
                            sseService.publish(errorResponse)
                        }
                        // Should never happened actually
                        else -> {
                            feedbacks.add(it)

                            eventPublisher.publishEvent(WebsocketResponseEvent(it))
                            sseService.publish(it)
                        }
                    }

                    // Remove domain from cmd
                    cmd.remove(domain.url)
                }
        }

        log.debug("Response: $feedbacks")
        return feedbacks.toMutableList()
    }

    suspend fun delete(domain: Domain): Feedback {
        sseService.eventName = "domain delete"
        val cmd = mutableListOf("uberspace", "web", "domain", "del", domain.url)

        val runner = Runner<Feedback>(env, sshService)

        runner.execute(Command(cmd), DeleteDomainParser())
        var feedback = Feedback("", "")

        runner.getBehaviourSubject()
            .doOnError { t ->
                // Should not happened
                log.error(t.message)
                feedback = t.message?.let { it -> Feedback("", it) }!!

                eventPublisher.publishEvent(WebsocketResponseEvent(feedback))
                sseService.publish(feedback)
            }.subscribe {
                eventPublisher.publishEvent(WebsocketResponseEvent(it))
                sseService.publish(it)
                feedback = it
            }

        return feedback
    }

    /**
     * Get all domains
     */
    fun getDomainList(): List<Domain> {
        return domains
    }
}