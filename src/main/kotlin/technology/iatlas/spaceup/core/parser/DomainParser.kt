package technology.iatlas.spaceup.core.parser

import org.slf4j.LoggerFactory
import technology.iatlas.spaceup.core.cmd.ParserInf
import technology.iatlas.spaceup.core.cmd.SshResponse
import technology.iatlas.spaceup.dto.Domain
import java.io.BufferedReader

class DomainParser : ParserInf<List<Domain>> {
    private val log = LoggerFactory.getLogger(DomainParser::class.java)

    private val domainList = mutableListOf<Domain>()

    override fun parseProcessOutput(processResponse: BufferedReader): List<Domain>{
        processResponse.lines().forEach {
            if (it.isNotBlank()) {
                val domain = Domain(it)
                domainList.add(domain)
            }
        }

        return domainList
    }

    override fun parseSshOutput(sshResponse: SshResponse): List<Domain> {
        sshResponse.stdout.split("\n")
            .filter {
                it != ""
            }.forEach {
            if (it.isNotBlank()) {
                val domain = Domain(it)
                domainList.add(domain)
            }
        }

        log.trace("Domains: $domainList")
        return domainList
    }
}