package technology.iatlas.spaceup.services

import io.micronaut.context.env.Environment
import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.Command
import technology.iatlas.spaceup.core.cmd.Runner
import technology.iatlas.spaceup.core.parser.CreateDomainParser
import technology.iatlas.spaceup.core.parser.DeleteDomainParser
import technology.iatlas.spaceup.core.parser.DomainParser
import technology.iatlas.spaceup.dto.Domain
import technology.iatlas.spaceup.dto.Feedback
import javax.inject.Singleton

@Singleton
class DomainService(private val env: Environment) {

    private val log = LoggerFactory.getLogger(DomainService::class.java)

    fun list(): List<Domain>? {
        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "list")
        return Runner<List<Domain>>(env).execute(Command(cmd), DomainParser())
    }

    fun add(domains: List<Domain>): Map<String, Feedback> {
        val cmd: MutableList<String> = mutableListOf("uberspace", "web", "domain", "add")
        val feedbacks: MutableMap<String, Feedback> = mutableMapOf()

        domains.forEach { domain ->
            // Add domain to cmd
            cmd.add(domain.url)
            val response: Feedback? = Runner<Feedback>(env)
                .execute(Command(cmd), CreateDomainParser())

            feedbacks[domain.url] = response!!

            // Remove domain from cmd
            cmd.remove(domain.url)
        }

        log.debug("Response: $feedbacks")
        return feedbacks
    }

    fun delete(domain: Domain): Feedback? {
        val cmd = mutableListOf("uberspace", "web", "domain", "del", domain.url)

        return Runner<Feedback>(env)
            .execute(Command(cmd), DeleteDomainParser())
    }
}