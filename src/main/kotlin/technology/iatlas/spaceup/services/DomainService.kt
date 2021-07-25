package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.CreateDomainParser
import technology.iatlas.spaceup.core.parser.DeleteDomainParser
import technology.iatlas.spaceup.core.parser.DomainParser
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.dto.Domain
import technology.iatlas.spaceup.dto.Feedback
import javax.inject.Singleton

@Singleton
class DomainService(
    private val env: Environment,
    private val sshService: SshService,
    private val wsBroadcaster: WsBroadcaster): WsServiceInf {
    private val log = LoggerFactory.getLogger(DomainService::class.java)

    private val domainListRunner = Runner<List<Domain>>(env, sshService)
    private var domains = listOf<Domain>()

    private val addDomainRunner = Runner<Feedback>(env, sshService)
    private val deleteDomainRunner = Runner<Feedback>(env, sshService)

    override val topic = "domains"

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
        val feedbacks = linkedSetOf<Feedback>()

        addDomainRunner.subject()
            .distinct()
            .subscribeBy(
                onNext = {
                    feedbacks.add(it)
                    wsBroadcaster.broadcast(it, feedbackTopic)
                },
                onError = { t ->
                    val error = t.message

                    val errorFeedback = Feedback("", "Could not add domain: ")
                    if (error != null) {
                        errorFeedback.error += error
                    }
                    feedbacks.add(errorFeedback)
                    wsBroadcaster.broadcast(errorFeedback, feedbackTopic)
                },
                onComplete = {
                    log.debug("Finished 'Add domain'")
                }
            )

        domains.forEach { domain ->
            // Add domain to cmd
            val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "add")
            cmd.add(domain.url)

            addDomainRunner.execute(Command(cmd), CreateDomainParser(domain.url))
        }

        log.debug("Response: $feedbacks")
        return feedbacks.toMutableList()
    }

    suspend fun delete(domain: Domain): Feedback {
        val cmd = mutableListOf("uberspace", "web", "domain", "del", domain.url)

        var feedback = Feedback("", "")
        deleteDomainRunner.subject()
            .distinct()
            .subscribeBy(
                onNext = {
                    feedback = it
                    wsBroadcaster.broadcast(it, feedbackTopic)
                },
                onError = { t ->
                    // Should not happened
                    log.error(t.message)
                    feedback = t.message?.let { it -> Feedback("", it) }!!
                    wsBroadcaster.broadcast(feedback, feedbackTopic)
                }
            )

        deleteDomainRunner.execute(Command(cmd), DeleteDomainParser(domain.url))
        return feedback
    }

    /**
     * Get all domains
     */
    fun list(): List<Domain> {
        wsBroadcaster.broadcastSync(domains, topic)
        return domains
    }
}