package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.dto.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.CreateDomainParser
import technology.iatlas.spaceup.core.parser.DeleteDomainParser
import technology.iatlas.spaceup.core.parser.DomainParser
import technology.iatlas.spaceup.dto.Domain
import technology.iatlas.spaceup.dto.Feedback
import javax.inject.Singleton

@Singleton
class DomainService(
    private val env: Environment,
    private val sshService: SshService,
    private val sseService: SseService<Feedback>
) {
    private val log = LoggerFactory.getLogger(DomainService::class.java)

    private val domainListRunner = Runner<List<Domain>>(env, sshService)
    private var domains = listOf<Domain>()

    private val addDomainRunner =  Runner<Feedback>(env, sshService)

    init {
        domainListRunner.getBehaviourSubject().subscribe {
            domains = it
        }


    }

    suspend fun updateDomainList() {
        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "list")
        domainListRunner.execute(Command(cmd), DomainParser())
    }

    suspend fun add(domains: List<Domain>): Map<String, Feedback> {
        sseService.eventName = "domain add"
        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "add")
        val feedbacks: MutableMap<String, Feedback> = mutableMapOf()

        domains.forEach { domain ->
            // Add domain to cmd
            cmd.add(domain.url)


            addDomainRunner.execute(Command(cmd), CreateDomainParser())

            addDomainRunner.getBehaviourSubject().subscribe {
                when {
                    it?.info!!.isNotEmpty() -> {
                        val infoResponse = Feedback(info = "${domain.url}: ${it.info}", "")
                        feedbacks[domain.url] = infoResponse

                        sseService.publish(infoResponse)
                    }
                    it.error.isNotEmpty() -> {
                        val errorResponse = Feedback(info = "", error = "${domain.url}: ${it.error}")
                        feedbacks[domain.url] = errorResponse

                        sseService.publish(errorResponse)
                    }
                    // Should never happened actually
                    else -> {
                        feedbacks[domain.url] = it
                        sseService.publish(it)
                    }
                }

                // Remove domain from cmd
                cmd.remove(domain.url)
            }
        }

        log.debug("Response: $feedbacks")
        return feedbacks
    }

    suspend fun delete(domain: Domain): Feedback {
        sseService.eventName = "domain delete"
        val cmd = mutableListOf("uberspace", "web", "domain", "del", domain.url)

        val runner = Runner<Feedback>(env, sshService)

        runner.execute(Command(cmd), DeleteDomainParser())
        var feedback = Feedback("", "")

        runner.getBehaviourSubject().subscribe {
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