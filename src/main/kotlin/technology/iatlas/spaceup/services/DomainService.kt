package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import io.reactivex.rxjava3.subjects.PublishSubject
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
    private val myDomains: PublishSubject<List<Domain>> = PublishSubject.create()
    private var domains = listOf<Domain>()

    init {
        myDomains.subscribe {
            domains = it
        }
    }

    private fun list(): List<Domain>? {
        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "list")
        return Runner<List<Domain>>(env, sshService).execute(Command(cmd), DomainParser())
    }

    fun add(domains: List<Domain>): Map<String, Feedback> {
        sseService.eventName = "domain add"
        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "add")
        val feedbacks: MutableMap<String, Feedback> = mutableMapOf()

        domains.forEach { domain ->
            // Add domain to cmd
            cmd.add(domain.url)
            val response: Feedback? = Runner<Feedback>(env, sshService)
                .execute(Command(cmd), CreateDomainParser())

            if (response?.info!!.isNotEmpty()) {
                val infoResponse = Feedback(info = "${domain.url}: ${response.info}", "")
                feedbacks[domain.url] = infoResponse

                sseService.publish(infoResponse)
            } else {
                feedbacks[domain.url] = response
                sseService.publish(response)
            }

            // Remove domain from cmd
            cmd.remove(domain.url)
        }

        log.debug("Response: $feedbacks")
        return feedbacks
    }

    fun delete(domain: Domain): Feedback {
        sseService.eventName="domain delete"
        val cmd = mutableListOf("uberspace", "web", "domain", "del", domain.url)

        val feedback = Runner<Feedback>(env, sshService)
            .execute(Command(cmd), DeleteDomainParser())
        sseService.publish(feedback!!)

        return feedback
    }

    /**
     * Update an observable list of domains
     */
    fun updateDomainList() {
        myDomains.onNext(list())
    }

    fun getDomainList(): List<Domain> {
        return domains
    }
}